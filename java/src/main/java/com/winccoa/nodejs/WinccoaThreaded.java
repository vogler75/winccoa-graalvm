package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WinccoaThreaded extends WinccoaAsync {

    private final ArrayBlockingQueue<Runnable> callbacks = new ArrayBlockingQueue<>(1000);

    public WinccoaThreaded() {
        scada.logInfo("Start thread.");
        new Thread(()->{
            while (true)
                try {
                    var next = callbacks.poll(10, TimeUnit.MILLISECONDS);
                    while (next != null) {
                        next.run();
                        next = callbacks.poll();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        }).start();
    }

    @Override
    public void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer) {
        try {
            callbacks.add(() -> scada.dpConnectCallback(uuid, names, values, answer));
        } catch (Exception e)   {
            scada.logSevere("dpConnectCallback error: " + e.getMessage());
        }
    }

    @Override
    public void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer) {
        try {
            callbacks.add(() -> scada.dpQueryConnectCallback(uuid, values, answer));
        } catch (Exception e)   {
            scada.logSevere("dpQueryConnectCallback error: " + e.getMessage());
        }
    }
}
