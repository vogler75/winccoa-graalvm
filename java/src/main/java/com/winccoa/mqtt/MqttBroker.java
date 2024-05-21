package com.winccoa.mqtt;

import com.winccoa.nodejs.WinccoaAsync;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

public class MqttBroker extends WinccoaAsync {
    Vertx vertx = Vertx.vertx();

    public void start() {
        var options = new MqttServerOptions()
                .setPort(1883)
                .setHost("0.0.0.0")
                .setUseWebSocket(false);

        var server = MqttServer.create(vertx, options);

        // Start a verticle for every incoming connection
        server.endpointHandler((endpoint)->{
            vertx.deployVerticle(new MqttVerticle(this, endpoint));
        });

        server.listen((result) -> {
            if (result.succeeded()) {
                logInfo("MQTT server started and listening on port " + server.actualPort());
            } else {
                logInfo("MQTT server error on start" + result.cause().getMessage());
            }
        });
    }
}
