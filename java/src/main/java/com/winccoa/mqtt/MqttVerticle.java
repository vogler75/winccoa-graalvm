package com.winccoa.mqtt;

import com.winccoa.nodejs.WinccoaAsync;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;

import java.util.HashMap;
import java.util.UUID;

public class MqttVerticle extends AbstractVerticle {
    private WinccoaAsync scada;
    private MqttEndpoint endpoint;

    private HashMap<String, String> subscribedTopics = new HashMap<>();

    public MqttVerticle(WinccoaAsync scada, MqttEndpoint endpoint) {
        this.scada = scada;
        this.endpoint = endpoint;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        scada.logInfo("Client connected: "+endpoint.clientIdentifier()+
                " CleanSession: "+endpoint.isCleanSession()+
                " Connected: "+endpoint.isConnected()+
                " Version: "+endpoint.protocolVersion()+
                " Protocol: "+endpoint.protocolName());
        endpoint.connectProperties().listAll().forEach((p)->{
            scada.logInfo(p.toString());
        });

        endpoint.accept(); // false .. no previous session present

        endpoint.disconnectHandler((handler) -> {
            scada.logInfo("Client disconnect: " + endpoint.clientIdentifier());
            unsubscribeAll();
            vertx.undeploy(this.deploymentID());
        });

        endpoint.closeHandler((handler) -> {
            scada.logInfo("Client close: " + endpoint.clientIdentifier());
            unsubscribeAll();
            vertx.undeploy(this.deploymentID());
        });

        endpoint.pingHandler((handler) -> {
            endpoint.pong();
        });

        endpoint.subscribeHandler((message) -> {
            message.topicSubscriptions().forEach((topic) -> {
                var uuid = UUID.randomUUID().toString();
                scada.dpConnect(uuid, topic.topicName(), true, (data) -> {
                    if (endpoint.isConnected()) {
                        data.asList().forEach((record) -> {
                            endpoint.publish(record.getKey(),
                                    Buffer.buffer(record.getValue().toString()),
                                    MqttQoS.AT_LEAST_ONCE, false /*isDup*/, false /* isRetain */);
                        });
                    }
                }).thenAccept((ok)-> {
                    if (ok) {
                        subscribedTopics.put(topic.topicName(), uuid)  ;
                    } else {
                        scada.logWarning("Subscribe to "+topic.topicName()+" failed.");
                    }
                });
            });
        });

        endpoint.unsubscribeHandler((message) -> {
            message.topics().forEach((topic) -> {
                var uuid = subscribedTopics.remove(topic);
                if (uuid != null) {
                    scada.dpDisconnect(uuid).thenAccept((ok)->{
                       if (!ok) {
                           scada.logWarning("Unsubscribe to "+topic+" failed.");
                       }
                    });
                }
            });
        });
    }

    private void unsubscribeAll() {
        subscribedTopics.forEach((topic, uuid)->scada.dpDisconnect(uuid));
    }
}
