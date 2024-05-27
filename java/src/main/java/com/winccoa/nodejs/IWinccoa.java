package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IWinccoa {
    void logInfo(String message);

    void logWarning(String message);

    void logSevere(String message);

    void exit();

    CompletableFuture<String> getSystemName();
    CompletableFuture<String> getSystemName(Integer systemId);

    CompletableFuture<Boolean> dpSet(String name, Object value);
    CompletableFuture<Boolean> dpSet(List<String> names, List<Object> values);
    CompletableFuture<Boolean> dpSetWait(String name, Object value);
    CompletableFuture<Boolean> dpSetWait(List<String> names, List<Object> values);
    CompletableFuture<Boolean> dpSetTimed(Date time, List<String> names, List<Object> values);
    CompletableFuture<Boolean> dpSetTimedWait(Date time, List<String> names, List<Object> values);

    CompletableFuture<Object> dpGet(String dps);
    CompletableFuture<Object> dpGet(List<String> dps);

    CompletableFuture<Boolean> dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback);
    CompletableFuture<Boolean> dpConnect(String uuid, List<String> names, Boolean answer, Consumer<DpConnectData> callback);

    CompletableFuture<Boolean> dpDisconnect(String uuid);
    CompletableFuture<Boolean> dpQueryConnectSingle(String uuid, String query, Boolean answer, Consumer<DpQueryConnectData> callback);
    CompletableFuture<Boolean> dpQueryDisconnect(String uuid);

    CompletableFuture<List<String>> dpNames(String dpPattern);
    CompletableFuture<List<String>> dpNames(String dpPattern, boolean ignoreCase);
    CompletableFuture<List<String>> dpNames(String dpPattern, String dpType, boolean ignoreCase);

    CompletableFuture<Integer> dpTypeCreate(String[][] elements, int[][] types);
    CompletableFuture<Integer> dpTypeDelete(String dpt);

    CompletableFuture<Boolean> dpCreate(String dpName, String dpType);
    CompletableFuture<Boolean> dpDelete(String dpName);

    void dpConnectCallback(String uuid, String[] names, Value[] values, boolean answer);
    void dpQueryConnectCallback(String uuid, Value[][] values, boolean answer);
}
