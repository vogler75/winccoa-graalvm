package com.winccoa.mqtt;

import com.winccoa.nodejs.DpConnectData;
import com.winccoa.nodejs.WinccoaAsync;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MqttVerticle extends AbstractVerticle {
    private WinccoaAsync scada;
    private MqttEndpoint endpoint;
    private HashMap<String, String> subscribedTopics = new HashMap<>();

    private Thread workerThread;
    private volatile long messageTimer = 0;
    private int messageCounter = 0;

//    private record PublishData(
//            String topic,
//            Buffer data
//    ) {}
//    private ArrayBlockingQueue<PublishData> dataQueue = new ArrayBlockingQueue<>(1000);

    public MqttVerticle(WinccoaAsync scada, MqttEndpoint endpoint) {
        this.scada = scada;
        this.endpoint = endpoint;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        scada.logInfo("Client connected: " + endpoint.clientIdentifier() +
                " CleanSession: " + endpoint.isCleanSession() +
                " Connected: " + endpoint.isConnected() +
                " Version: " + endpoint.protocolVersion() +
                " Protocol: " + endpoint.protocolName());

        endpoint.accept(false); // false .. no previous session present

        messageTimer = vertx.setPeriodic(1000, id -> {
            messageCounter = 0;
        });

        endpoint.disconnectHandler((handler) -> {
            scada.logInfo("Client disconnect: " + endpoint.clientIdentifier());
            cleanupSession();

        });

        endpoint.closeHandler((handler) -> {
            scada.logInfo("Client close: " + endpoint.clientIdentifier());
            cleanupSession();
        });

        endpoint.pingHandler((handler) -> {
            endpoint.pong();
        });

//        workerThread = new Thread(()->{
//            PublishData data;
//            while (true) {
//                try {
//                    data = dataQueue.poll(10, TimeUnit.MILLISECONDS);
//                    while (data != null) {
//                        publishData(data);
//                        data = dataQueue.poll();
//                    }
//                } catch (InterruptedException e) {
//                    scada.logSevere(e.getMessage());
//                }
//            }
//        });
//        workerThread.start();

        endpoint.subscribeHandler((message) -> {
            message.topicSubscriptions().forEach((topic) -> {
                var uuid = UUID.randomUUID().toString();
                scada.dpConnect(uuid, topic.topicName(), true, (data) -> {
                    data.asList().forEach((item)->{
//                        dataQueue.add(new PublishData(item.getKey(), Buffer.buffer(item.getValue().toString())));
                        if (endpoint.isConnected()) {
                            messageCounter++;
                            endpoint.publish(topic.topicName(),
                                    Buffer.buffer(item.getValue().toString()),
                                    MqttQoS.AT_LEAST_ONCE, false /*isDup*/, false /* isRetain */);
                        }
                    });
                }).thenAccept((ok) -> {
                    if (ok) {
                        subscribedTopics.put(topic.topicName(), uuid);
                    } else {
                        scada.logWarning("Subscribe to " + topic.topicName() + " failed.");
                    }
                });
            });
        });

        endpoint.unsubscribeHandler((message) -> {
            message.topics().forEach((topic) -> {
                var uuid = subscribedTopics.remove(topic);
                if (uuid != null) {
                    scada.dpDisconnect(uuid).thenAccept((ok) -> {
                        if (!ok) {
                            scada.logWarning("Unsubscribe to " + topic + " failed.");
                        }
                    });
                }
            });
        });
    }

    private void cleanupSession() {
        unsubscribeAll();
        vertx.cancelTimer(messageTimer);
        vertx.undeploy(this.deploymentID());
    }

//    private void publishData(PublishData data) {
//        if (endpoint.isConnected()) {
//            messageCounter++;
//            endpoint.publish(data.topic(),
//                    data.data,
//                    MqttQoS.AT_LEAST_ONCE, false /*isDup*/, false /* isRetain */);
//        }
//    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }


    private void unsubscribeAll() {
        subscribedTopics.forEach((topic, uuid) -> scada.dpDisconnect(uuid));
    }
}
