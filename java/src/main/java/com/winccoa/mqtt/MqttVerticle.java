package com.winccoa.mqtt;

import com.winccoa.nodejs.WinccoaAsync;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;

public class MqttVerticle extends AbstractVerticle {
    WinccoaAsync scada;
    MqttEndpoint endpoint;

    public MqttVerticle(WinccoaAsync scada, MqttEndpoint endpoint) {
        this.scada = scada;
        this.endpoint = endpoint;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        scada.logInfo("Client connected.");
        endpoint.accept(false); // false .. no previous session present

        endpoint.disconnectHandler((handler)-> {
            scada.logInfo("Client disconnect.");
            vertx.undeploy(this.deploymentID());
        });

        endpoint.closeHandler((handler)-> {
            scada.logInfo("Client close.");
            vertx.undeploy(this.deploymentID());
        });

        endpoint.subscribeHandler((message)->{
            message.topicSubscriptions().forEach((topic)->{
                scada.dpConnect(topic.topicName(), topic.topicName(), true, (data)->{
                    scada.logInfo("Publish data.");
                    for (int i = 0; i<data.names().length; i++) {
                        endpoint.publish(topic.topicName(),
                                Buffer.buffer(data.values()[i].toString()),
                                MqttQoS.AT_LEAST_ONCE,  false /*isDup*/, false /* isRetain */);
                    }
                });
            });
        });
    }
}
