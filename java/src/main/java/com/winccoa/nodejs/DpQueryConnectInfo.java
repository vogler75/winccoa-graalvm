package com.winccoa.nodejs;

import java.util.function.Consumer;

public record DpQueryConnectInfo(
        long id,
        Consumer<DpQueryConnectData> callback
) { }