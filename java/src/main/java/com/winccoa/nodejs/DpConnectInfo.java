package com.winccoa.nodejs;

import java.util.function.Consumer;

public record DpConnectInfo(
        long id,
        Consumer<DpConnectData> callback
) { }