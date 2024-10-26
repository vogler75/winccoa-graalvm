package com.winccoa.logger;

import com.winccoa.nodejs.DpQueryConnectData;
import com.winccoa.nodejs.WinccoaAsync;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import net.snowflake.ingest.streaming.InsertValidationResponse;
import net.snowflake.ingest.streaming.OpenChannelRequest;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestChannel;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestClientFactory;
import org.graalvm.polyglot.Value;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class SnowflakeVerticle extends AbstractVerticle {

    private final WinccoaAsync scada;
    private SnowflakeStreamingIngestChannel channel;

    public SnowflakeVerticle(WinccoaAsync scada) {
        this.scada = scada;
    }

    @Override
    public void start() throws Exception {
        var jsonFilePath = Paths.get("snowflake.json");
        var jsonString = new String(Files.readAllBytes(jsonFilePath));
        var jsonObject = new JsonObject(jsonString);

        var props = new Properties();

        var privateKeyPath = Paths.get(jsonObject.getString("private_key_path", "rsa_key.p8"));
        var privateKeyContent = new String(Files.readAllBytes(privateKeyPath));
        var privateKey = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        var database = jsonObject.getString("database");
        var schema = jsonObject.getString("schema");
        var table = jsonObject.getString("table");
        var scadaQuery = jsonObject.getString("scada_query");
        var scadaSystem = jsonObject.getString("scada_system");

        props.put("account", jsonObject.getString("account"));
        props.put("url", jsonObject.getString("url"));
        props.put("user", jsonObject.getString("user"));
        props.put("private_key", privateKey);
        props.put("role", jsonObject.getString("role"));
        props.put("scheme", jsonObject.getString("scheme"));
        props.put("port", jsonObject.getInteger("port").toString());


        // Create a streaming ingest client
        var client = SnowflakeStreamingIngestClientFactory.builder("client1").setProperties(props).build();

        // Open a channel
        var request = OpenChannelRequest.builder("channel1")
                .setDBName(database)
                .setSchemaName(schema)
                .setTableName(table)
                .setOnErrorOption(OpenChannelRequest.OnErrorOption.CONTINUE)
                .build();

        // Open a streaming ingest channel from the given client
        channel = client.openChannel(request);


        var id = UUID.randomUUID().toString();
        var sql = "SELECT '_online.._value', '_online.._stime', '_online.._status' "+scadaQuery;
        scada.dpQueryConnectSingle(id, sql, false, data -> {
            //scada.logInfo("Callback Query: " + data.values().length);

            for (var i = 0; i < data.values().length; i++) {
                if (i == 0) continue;

                var row = data.values()[i];
                //scada.logInfo("+ "+Arrays.toString(row));

                var dp = (Value) row[0];
                var value = (Value) row[1];
                var time = (Value) row[2];
                var status = (Value) row[3];

                var record = new HashMap<String, Object>();
                record.put("SYSTEM", scadaSystem);
                record.put("DATAPOINT", dp.toString());
                record.put("SOURCETIME", time.asInstant().toString());
                record.put("SERVERTIME", Instant.now().toString());
                record.put("STRINGVALUE", value.isString() ? value.asString() : null);
                record.put("NUMERICVALUE", value.isNumber() ? value.asDouble() : null);
                record.put("STATUS", status.toString());

                scada.logInfo("Record: " + record);

                if (channel != null) {
                    InsertValidationResponse response = channel.insertRow(record, String.valueOf(i));
                    if (response.hasErrors()) {
                        scada.logSevere("Error inserting row: " + response.getInsertErrors().getFirst().getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        // Close the channel, the function internally will make sure everything is committed (or throw an exception if there is any issue)
        if (channel != null) {
            channel.close().get();
        }
    }
}
