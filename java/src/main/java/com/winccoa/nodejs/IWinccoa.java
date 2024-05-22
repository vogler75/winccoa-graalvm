package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IWinccoa {
    void logInfo(String message);

    void logWarning(String message);

    void logSevere(String message);

    void exit();

    CompletableFuture<Boolean> dpSet(Object... arguments);

    CompletableFuture<Boolean> dpSetWait(Object... arguments);

    CompletableFuture<Object> dpGet(String dps);

    CompletableFuture<Object> dpGet(List<String> dps);

    CompletableFuture<Boolean> dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback);

    CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback);

    void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer);

    CompletableFuture<Boolean> dpDisconnect(String uuid);

    CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback);

    void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer);

    CompletableFuture<Boolean> dpQueryDisconnect(String uuid);

    CompletableFuture<List<String>> dpNames(String dpPattern, String dpType, boolean ignoreCase);

    CompletableFuture<Integer> dpTypeCreate(String[][] elements, int[][] types);
}
