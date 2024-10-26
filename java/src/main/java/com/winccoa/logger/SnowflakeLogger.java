package com.winccoa.logger;

import com.winccoa.nodejs.WinccoaAsync;

import io.vertx.core.Vertx;

public class SnowflakeLogger extends WinccoaAsync {
    Vertx vertx = Vertx.vertx();

    public SnowflakeLogger() {
        vertx.deployVerticle(new SnowflakeVerticle(this));
    }
}