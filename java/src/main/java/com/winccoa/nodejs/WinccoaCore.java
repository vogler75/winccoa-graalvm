package com.winccoa.nodejs;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

public class WinccoaCore {
    private final String jsLangId = "js";
    private final Context ctx = Context.getCurrent();

    private final HashMap<String, DpConnectInfo> dpConnects = new HashMap<>();
    private final HashMap<String, DpQueryConnectInfo> dpQueryConnects = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Please run it from Node.js.");
    }

    public void test() {
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

        logInfo("Set 0 "+ dpSet("ExampleDP_Arg1.", 0));

        var promise1 = dpSetWait("ExampleDP_Arg1.", 1).thenAccept((value)-> logInfo("Set 1 Done!"));

        var promise2 = dpSetWait("ExampleDP_Arg1.", 2).thenAccept((value)-> logInfo("Set 2 Done!"));

        var promise3 = dpSetWait(
                Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."),
                Arrays.asList(3,3)
        ).thenAccept((value)-> logInfo("Set 3 Done!"));

        CompletableFuture.allOf(promise1, promise2, promise3).thenAccept((unused)-> {
            logInfo("Disconnect: "+dpDisconnect(id1));
            logInfo("Disconnect: "+dpDisconnect(id2));
            logInfo("Disconnect: "+dpQueryDisconnect(id3));
            dpGet("ExampleDP_Arg1.").thenAccept((value)->logInfo("dpGet: "+value.toString()));
            dpGet(Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2.")).thenAccept((value)->logInfo("dpGet: "+value.toString()));
        });

        dpSet(Arrays.asList("ExampleDP_Rpt1.","ExampleDP_Rpt2."), Arrays.asList(3, 4));

        logInfo("Test End.");
    }

    public void logInfo(String message) {
        ctx.eval(jsLangId, "scada.logInfo('"+message+"')");
    }

    public void logWarning(String message) {
        ctx.eval(jsLangId, "scada.logWarning('"+message+"')");
    }

    public void logSevere(String message) {
        ctx.eval(jsLangId, "scada.logSevere('"+message+"')");
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpSet = ctx.eval(jsLangId, """
        (function(names, values) {
            console.log(`Java::dpSet(${names},${values})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            if (Array.isArray(values)) values = values.map(item => String(item));
            return scada.dpSet(names, values);
        })
        """);

    public Boolean dpSet(Object... arguments) {
        Value result = jsDpSet.execute(arguments);
        return result.asBoolean();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpSetWait = ctx.eval(jsLangId, """
        (function(names, values) {
            console.log(`Java::dpSetWait(${names},${values})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            if (Array.isArray(values)) values = values.map(item => String(item));
            return scada.dpSetWait(names, values);
        })
        """);

    public CompletableFuture<Boolean> dpSetWait(Object... arguments) {
        Value promise = jsDpSetWait.execute(arguments);  // js promise
        var future = new CompletableFuture<Boolean>(); // java promise
        Consumer<Boolean> then = future::complete; // = (values) -> future.complete(values);
        promise.invokeMember("then", then);
        return future;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpGet = ctx.eval(jsLangId, """
        (function(names) {
            console.log(`Java::dpGet(${names})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            return scada.dpGet(names);
        })
        """);

    public CompletableFuture<Object> dpGet(String dps) {
        Value promise = jsDpGet.execute(dps);  // js promise
        var future = new CompletableFuture<>(); // java promise
        Consumer<Object> then = future::complete; // = (values) -> future.complete(values);
        promise.invokeMember("then", then);
        return future;
    }

    public CompletableFuture<Object> dpGet(List<String> dps) {
        Value promise = jsDpGet.execute(dps);  // js promise
        var future = new CompletableFuture<>(); // java promise
        Consumer<Object> then = future::complete; // = (values) -> future.complete(values);
        promise.invokeMember("then", then);
        return future;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpConnect = ctx.eval(jsLangId, """
        (function(uuid, names, answer) {
            console.log(`Java::dpConnect(${uuid},${names},${answer})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            return node.dpConnect(uuid, names, answer);
        })
        """);

    public boolean dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback) {
        return dpConnect(uuid, Collections.singletonList(name), answer, callback);
    }

    public boolean dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback) {
        long id = jsDpConnect.execute(uuid, names, answer).asLong();
        if (id >= 0) {
            dpConnects.put(uuid, new DpConnectInfo(id, callback));
            return true;
        } else {
            return false;
        }
    }

    public void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer) {
        //logInfo("Java::dpConnectCallback "+uuid+" => "+names+" => "+values+" answer: "+answer);
        Optional.ofNullable(dpConnects.get(uuid))
                .ifPresent((data)-> data.callback().accept(new DpConnectData(answer, names, values)));
    }

    private final Value jsDpDisconnect = ctx.eval(jsLangId, """
        (function(id) {
            console.log(`Java::dpDisconnect(${id})`);
            return node.dpDisconnect(id);
        })
        """);

    public boolean dpDisconnect(String uuid) {
        if (dpConnects.containsKey(uuid)) {
            DpConnectInfo data = dpConnects.get(uuid);
            jsDpDisconnect.execute(data.id());
            dpConnects.remove(uuid);
            return true;
        } else {
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpQueryConnectSingle = ctx.eval(jsLangId, """
        (function(uuid, query, answer) {
            console.log(`Java::jsDpQueryConnectSingle(${uuid},${query},${answer})`);
            return node.dpQueryConnectSingle(uuid, query, answer);
        })
        """);

    public boolean dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback) {
        long id = jsDpQueryConnectSingle.execute(uuid, query, answer).asLong();
        if (id >= 0) {
            dpQueryConnects.put(uuid, new DpQueryConnectInfo(id, callback));
            return true;
        } else {
            return false;
        }
    }

    public void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer) {
        //logInfo("Java::dpConnectCallback "+uuid+" => "+names+" => "+values+" answer: "+answer);
        Optional.ofNullable(dpQueryConnects.get(uuid))
                .ifPresent((data)-> data.callback().accept(new DpQueryConnectData(answer, values)));
    }

    private final Value jsDpQueryDisconnect = ctx.eval(jsLangId, """
        (function(id) {
            console.log(`Java::dpQueryDisconnect(${id})`);
            return node.dpQueryDisconnect(id);
        })
        """);

    public boolean dpQueryDisconnect(String uuid) {
        if (dpQueryConnects.containsKey(uuid)) {
            DpQueryConnectInfo data = dpQueryConnects.get(uuid);
            jsDpQueryDisconnect.execute(data.id());
            dpQueryConnects.remove(uuid);
            return true;
        } else {
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsExit = ctx.eval(jsLangId, """
        (function(id, dp) {
            //console.log(`Java::exit()`);
            scada.exit();
        })
        """);

    public void exit() {
        jsExit.execute();
    }
}