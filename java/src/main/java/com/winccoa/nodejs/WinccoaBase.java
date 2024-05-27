package com.winccoa.nodejs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class WinccoaBase implements IWinccoa {

    @Override
    public CompletableFuture<Object> dpGet(String dps) {
        return dpGet(List.of(dps));
    }

    @Override
    public CompletableFuture<Boolean> dpSet(String name, Object value) {
        return dpSet(List.of(name), List.of(value));
    }

    @Override
    public CompletableFuture<Boolean> dpSetWait(String name, Object value) {
        return dpSetWait(List.of(name), List.of(value));
    }

    @Override
    public CompletableFuture<Boolean> dpConnect(String uuid, String name, Boolean answer, Consumer<DpConnectData> callback) {
        return dpConnect(uuid, List.of(name), answer, callback);
    }

    @Override
    public CompletableFuture<List<String>> dpNames(String dpPattern) {
        return dpNames(dpPattern, "", false);
    }

    @Override
    public CompletableFuture<List<String>> dpNames(String dpPattern, boolean ignoreCase) {
        return dpNames(dpPattern, "", ignoreCase);
    }
}
