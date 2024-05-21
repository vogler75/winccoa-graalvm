package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

public record DpQueryConnectData(
        boolean answer,
        Value[][] values
) { }