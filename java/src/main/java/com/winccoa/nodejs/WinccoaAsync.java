package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WinccoaAsync extends WinccoaCore implements IWinccoa {
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);

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
        queue.add(() -> super.logInfo(message));
    }

    @Override
    public void logWarning(String message) {
        queue.add(() -> super.logWarning(message));
    }

    @Override
    public void logSevere(String message) {
        queue.add(() -> super.logSevere(message));
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public void exit() {
        queue.add(() -> super.exit());
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpSet(List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpSet(names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetWait(List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpSetWait(names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetTimed(Date time, List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpSetTimed(time, names, values).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetTimedWait(Date time, List<String> names, List<Object> values) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpSetTimedWait(time, names, values).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Object> dpGet(List<String> dps) {
        var promise = new CompletableFuture<Object>();
        queue.add(() -> super.dpGet(dps).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpConnect(uuid, names, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpQueryConnectSingle(uuid, query, answer, callback).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpQueryDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpQueryDisconnect(uuid).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<List<String>> dpNames(String dpPattern, String dpType, boolean ignoreCase) {
        var promise = new CompletableFuture<List<String>>();
        queue.add(() -> super.dpNames(dpPattern, dpType, ignoreCase).thenAccept(promise::complete));
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Integer> dpTypeCreate(String[][] elements, int[][] types) {
        var promise = new CompletableFuture<Integer>();
        queue.add(() -> super.dpTypeCreate(elements, types).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Integer> dpTypeDelete(String dpt) {
        var promise = new CompletableFuture<Integer>();
        queue.add(() -> super.dpTypeDelete(dpt).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpCreate(String dpName, String dpType) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpCreate(dpName, dpType).thenAccept(promise::complete));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpDelete(String dpName) {
        var promise = new CompletableFuture<Boolean>();
        queue.add(() -> super.dpDelete(dpName).thenAccept(promise::complete));
        return promise;
    }
}