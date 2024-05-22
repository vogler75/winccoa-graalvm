package com.winccoa.nodejs;

import io.netty.util.internal.EmptyArrays;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

public class WinccoaCore implements IWinccoa {
    private final String jsLangId = "js";

    public final Context ctx = Context.getCurrent();

    private final HashMap<String, DpConnectInfo> dpConnects = new HashMap<>();
    private final HashMap<String, DpQueryConnectInfo> dpQueryConnects = new HashMap<>();

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsExit = ctx.eval(jsLangId, """
        (function(id, dp) {
            console.log(`Java::exit()`);
            scada.exit();
        })
        """);

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpSet = ctx.eval(jsLangId, """
        (function(names, values) {
            console.log(`Java::dpSet(${names},${values})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            if (Array.isArray(values)) values = values.map(item => String(item));
            return scada.dpSet(names, values);
        })
        """);

    private final Value jsDpSetWait = ctx.eval(jsLangId, """
        (function(names, values) {
            console.log(`Java::dpSetWait(${names},${values})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            if (Array.isArray(values)) values = values.map(item => String(item));
            return scada.dpSetWait(names, values);
        })
        """);

    private final Value jsDpGet = ctx.eval(jsLangId, """
        (function(names) {
            console.log(`Java::dpGet(${names})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            return scada.dpGet(names);
        })
        """);

    private final Value jsDpConnect = ctx.eval(jsLangId, """
        (function(uuid, names, answer) {
            console.log(`Java::dpConnect(${uuid},${names},${answer})`);
            if (Array.isArray(names)) names = names.map(item => String(item));
            return node.dpConnect(uuid, names, answer);
        })
        """);
        
    private final Value jsDpDisconnect = ctx.eval(jsLangId, """
        (function(id) {
            console.log(`Java::dpDisconnect(${id})`);
            return node.dpDisconnect(id);
        })
        """);

    private final Value jsDpQueryConnectSingle = ctx.eval(jsLangId, """
        (function(uuid, query, answer) {
            console.log(`Java::jsDpQueryConnectSingle(${uuid},${query},${answer})`);
            return node.dpQueryConnectSingle(uuid, query, answer);
        })
        """);

    private final Value jsDpQueryDisconnect = ctx.eval(jsLangId, """
        (function(id) {
            console.log(`Java::dpQueryDisconnect(${id})`);
            return node.dpQueryDisconnect(id);
        })
        """);

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpNames = ctx.eval(jsLangId, """
        (function(dpPattern, dpType, ignoreCase) {
            console.log(`Java::dpNames(${dpPattern},${dpType},${ignoreCase})`);
            if (!dpPattern) dpPattern=undefined;
            if (!dpType) dpType=undefined;
            if (!ignoreCase) ignoreCase=undefined;
            return scada.dpNames(dpPattern, dpType, ignoreCase);
        })
        """);

    // -----------------------------------------------------------------------------------------------------------------

    private final Value jsDpTypeCreate = ctx.eval(jsLangId, """
        (function(elements, types) {
            console.log(`Java::dpTypeCreate(${elements},${types})`);
            return node.dpTypeCreate(elements, types);
        })
        """);

    private final Value jsDpCreate = ctx.eval(jsLangId, """
        (function(dpName, dpType) {
            console.log(`Java::dpCreate(${dpName},${dpType})`);
            return scada.dpCreate(dpName, dpType);
        })
        """);

    // -----------------------------------------------------------------------------------------------------------------
        
    @Override
    public void logInfo(String message) {
        ctx.eval(jsLangId, "scada.logInfo('"+message+"')");
    }

    @Override
    public void logWarning(String message) {
        ctx.eval(jsLangId, "scada.logWarning('"+message+"')");
    }

    @Override
    public void logSevere(String message) {
        ctx.eval(jsLangId, "scada.logSevere('"+message+"')");
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void exit() {
        jsExit.execute();
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpSet(Object... arguments) {
        var promise = new CompletableFuture<Boolean>();
        Value result = jsDpSet.execute(arguments);
        promise.complete(result.asBoolean());
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> dpSetWait(Object... arguments) {
        Value promise = jsDpSetWait.execute(arguments);  // js promise
        var future = new CompletableFuture<Boolean>(); // java promise
        Consumer<Boolean> then = future::complete; // = (result) -> future.complete(result);
        promise.invokeMember("then", then);
        return future;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Object> dpGet(String dps) {
        Value promise = jsDpGet.execute(dps);  // js promise
        var future = new CompletableFuture<>(); // java promise
        Consumer<Object> then = future::complete; // = (result) -> future.complete(result);
        promise.invokeMember("then", then);
        return future;
    }

    @Override
    public CompletableFuture<Object> dpGet(List<String> dps) {
        Value promise = jsDpGet.execute(dps);  // js promise
        var future = new CompletableFuture<>(); // java promise
        Consumer<Object> then = future::complete; // = (result) -> future.complete(result);
        promise.invokeMember("then", then);
        return future;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback) {
        return dpConnect(uuid, Collections.singletonList(name), answer, callback);
    }

    @Override
    public CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();

        long id = jsDpConnect.execute(uuid, names, answer).asLong();
        if (id >= 0) {
            dpConnects.put(uuid, new DpConnectInfo(id, callback));
            promise.complete(true);
        } else {
            promise.complete(false);
        }
        return promise;
    }

    @Override
    public void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer) {
        //logInfo("Java::dpConnectCallback "+uuid+" => "+names+" => "+values+" answer: "+answer);
        Optional.ofNullable(dpConnects.get(uuid))
                .ifPresent((data)-> data.callback().accept(new DpConnectData(answer, names, values)));
    }

    @Override
    public CompletableFuture<Boolean> dpDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        if (dpConnects.containsKey(uuid)) {
            DpConnectInfo data = dpConnects.get(uuid);
            jsDpDisconnect.execute(data.id());
            dpConnects.remove(uuid);
            promise.complete(true);
        } else {
            promise.complete(false);
        }
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback) {
        var promise = new CompletableFuture<Boolean>();
        long id = jsDpQueryConnectSingle.execute(uuid, query, answer).asLong();
        if (id >= 0) {
            dpQueryConnects.put(uuid, new DpQueryConnectInfo(id, callback));
            promise.complete(true);
        } else {
            promise.complete(false);
        }
        return promise;
    }

    @Override
    public void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer) {
        //logInfo("Java::dpConnectCallback "+uuid+" => "+names+" => "+values+" answer: "+answer);
        Optional.ofNullable(dpQueryConnects.get(uuid))
                .ifPresent((data)-> data.callback().accept(new DpQueryConnectData(answer, values)));
    }

    @Override
    public CompletableFuture<Boolean> dpQueryDisconnect(String uuid) {
        var promise = new CompletableFuture<Boolean>();
        if (dpQueryConnects.containsKey(uuid)) {
            DpQueryConnectInfo data = dpQueryConnects.get(uuid);
            jsDpQueryDisconnect.execute(data.id());
            dpQueryConnects.remove(uuid);
            promise.complete(true);
        } else {
            promise.complete(false);
        }
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public CompletableFuture<List<String>> dpNames(String dpPattern, String dpType, boolean ignoreCase) {
        var promise = new CompletableFuture<List<String>>();
        var values = jsDpNames.execute(dpPattern, dpType, ignoreCase);
        var result = new ArrayList<String>();
        for (long i=0; i<values.getArraySize(); i++)
            result.add(values.getArrayElement(i).asString());
        promise.complete(result);
        return promise;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public CompletableFuture<Integer> dpTypeCreate(String[][] elements, int[][] types) {
        var promise = jsDpTypeCreate.execute(elements, types);
        var future = new CompletableFuture<Integer>(); // java promise
        Consumer<Double> then = (result) -> future.complete((int)Math.ceil(result)); // = (result) -> future.complete(result);
        promise.invokeMember("then", then);
        return future;
    }

    //    //dpCreate(dpeName, dpType, systemId?, dpId?): Promise<boolean>
    @Override
    public CompletableFuture<Boolean> dpCreate(String dpName, String dpType) {
        var promise = jsDpCreate.execute(dpName, dpType);
        var future = new CompletableFuture<Boolean>(); // java promise
        Consumer<Boolean> then = future::complete; // = (result) -> future.complete(result);
        promise.invokeMember("then", then);
        return future;
    }
}