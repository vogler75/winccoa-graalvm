package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WinccoaAsync extends Winccoa implements IWinccoa {
    protected final WinccoaCore scada = new WinccoaCore();
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    // Called from node.js every x ms.
    public boolean loop() {
        var next = queue.poll();
        for (int i=0; i<1000 && next != null; i++) {
            next.run();
            next = queue.poll();
        }
        return next != null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void logInfo(String message) {
        queue.add(() -> scada.logInfo(message));
    }

    @Override
    public void logWarning(String message) {
        queue.add(() -> scada.logWarning(message));
    }

    @Override
    public void logSevere(String message) {
        queue.add(() -> scada.logSevere(message));
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public void exit() {
        queue.add(() -> scada.exit());
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpSet(List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSet(names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetWait(List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSetWait(names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetTimed(Date time, List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSetTimed(time, names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetTimedWait(Date time, List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpSetTimedWait(time, names, values).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Object> dpGet(List<String> dps) {
        var promise = new CompletableFuture<Object>();
        queue.add(() -> scada.dpGet(dps).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpConnect(uuid, names, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer) {
        scada.dpConnectCallback(uuid, names, values, answer);
    }

    @Override
    public CompletableFuture<Boolean> dpDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpQueryConnectSingle(uuid, query, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer) {
        scada.dpQueryConnectCallback(uuid, values, answer);
    }

    @Override
    public CompletableFuture<Boolean> dpQueryDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpQueryDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<List<String>> dpNames(String dpPattern, String dpType, boolean ignoreCase) {
        var promise = new CompletableFuture<List<String>>();
        queue.add(() -> scada.dpNames(dpPattern, dpType, ignoreCase).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Integer> dpTypeCreate(String[][] elements, int[][] types) {
        var promise = new CompletableFuture<Integer>();
        queue.add(() -> scada.dpTypeCreate(elements, types).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Integer> dpTypeDelete(String dpt) {
        var promise = new CompletableFuture<Integer>();
        queue.add(() -> scada.dpTypeDelete(dpt).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpCreate(String dpName, String dpType) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpCreate(dpName, dpType).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpDelete(String dpName) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> scada.dpDelete(dpName).thenAccept(promise::complete));
        return promise;
    }
}