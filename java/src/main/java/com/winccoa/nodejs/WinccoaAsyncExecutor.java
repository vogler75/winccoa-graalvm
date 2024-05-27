package com.winccoa.nodejs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WinccoaAsyncExecutor extends WinccoaAsync {

    private final ExecutorService executor;

    public WinccoaAsyncExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void dpConnectCallbackExecute(DpConnectInfo connect, DpConnectData data) {
        executor.execute(()->connect.callback().accept(data));
    }

    @Override
    public void dpQueryConnectCallbackExecute(DpQueryConnectInfo connect, DpQueryConnectData data) {
        executor.execute(()->connect.callback().accept(data));
    }
}
