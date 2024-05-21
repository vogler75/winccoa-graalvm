package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WinccoaAsync implements IWinccoa {
    protected final WinccoaCore scada = new WinccoaCore();
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    // Called from node.js every x ms.
    public boolean loop() {
        var next = queue.poll();
        if (next != null) {
            next.run();
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void logInfo(String message) {
        queue.add(() -> scada.logInfo(message));
    }

    public void logWarning(String message) {
        queue.add(() -> scada.logWarning(message));
    }

    public void logSevere(String message) {
        queue.add(() -> scada.logSevere(message));
    }    

    // -----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> dpSet(Object... arguments) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSet(arguments).thenAccept(promise::complete));
        return promise;
    }

    public CompletableFuture<Boolean> dpSetWait(Object... arguments) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSetWait(arguments).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Object> dpGet(String dps) {
        var promise = new CompletableFuture<Object>();
        queue.add(() -> scada.dpGet(dps).thenAccept(promise::complete));
        return promise;
    }

    public CompletableFuture<Object> dpGet(List<String> dps) {
        var promise = new CompletableFuture<Object>();
        queue.add(() -> scada.dpGet(dps).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback) {
        return dpConnect(uuid, Collections.singletonList(name), answer, callback);
    }

    public CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpConnect(uuid, names, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    public void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer) {
        scada.dpConnectCallback(uuid, names, values, answer);
    }

    public CompletableFuture<Boolean> dpDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpQueryConnectSingle(uuid, query, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    public void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer) {
        scada.dpQueryConnectCallback(uuid, values, answer);
    }

    public CompletableFuture<Boolean> dpQueryDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpQueryDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void exit() {
        queue.add(() -> scada.exit());
    }
}