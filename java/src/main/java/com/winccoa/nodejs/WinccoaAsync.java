package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WinccoaAsync implements IWinccoa {

    WinccoaCore scada = new WinccoaCore();

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public void test() {
        new Thread(() -> {
            logInfo("Test Start");

            var id1 = UUID.randomUUID().toString();
            dpConnect(id1, "ExampleDP_Rpt1.", true, (data) -> {
                logInfo("Callback Single "+ Arrays.toString(data.names()) +" "+ Arrays.toString(data.values()));
            });

            var id2 = UUID.randomUUID().toString();
            dpConnect(id2, Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."), true, (data) -> {
                logInfo("Callback Array "+ Arrays.toString(data.names()) +" "+ Arrays.toString(data.values()));
            });

            var id3 = UUID.randomUUID().toString();
            var sql = "SELECT '_online.._value' FROM '*' WHERE _DPT= \"ExampleDP_Float\"";
            dpQueryConnectSingle(id3, sql, true, (data) -> {
                logInfo("Callback Query: "+data.values().length);
                List.of(data.values()).forEach((row)-> {
                    logInfo("+ "+Arrays.toString(row));
                });
            });

            var promise0 = dpSet("ExampleDP_Arg1.", 0).thenAccept((value)-> logInfo("Set 0 Doen!"));

            var promise1 = dpSetWait("ExampleDP_Arg1.", 1).thenAccept((value)-> logInfo("Set 1 Done!"));

            var promise2 = dpSetWait("ExampleDP_Arg1.", 2).thenAccept((value)-> logInfo("Set 2 Done!"));

            var promise3 = dpSetWait(
                    Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."),
                    Arrays.asList(3,3)
            ).thenAccept((value)-> logInfo("Set 3 Done!"));

            CompletableFuture.allOf(promise0, promise1, promise2, promise3).thenAccept((unused)-> {
                logInfo("Disconnect: "+dpDisconnect(id1));
                logInfo("Disconnect: "+dpDisconnect(id2));
                logInfo("Disconnect: "+dpQueryDisconnect(id3));
                dpGet("ExampleDP_Arg1.").thenAccept((value)->logInfo("dpGet: "+value.toString()));
                dpGet(Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2.")).thenAccept((value)->logInfo("dpGet: "+value.toString()));
            });

            dpSet(Arrays.asList("ExampleDP_Rpt1.","ExampleDP_Rpt2."), Arrays.asList(3, 4))
                    .thenAccept((value)->logInfo("Set Array "+value));

            logInfo("Test End.");

        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            exit();
        }).start();
    }

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