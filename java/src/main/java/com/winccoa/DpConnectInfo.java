package com.winccoa;

import java.util.function.Consumer;

public record DpConnectInfo(
        long id,
        Consumer<DpConnectData> callback
) { }