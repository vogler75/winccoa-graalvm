package com.winccoa.mqtt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Main extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        router.get("/").handler(this::handleRoot);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void handleRoot(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("Hello, Vert.x!");
    }
}
