package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

public record DpConnectData(
        boolean answer,
        String[] names,
        Value[] values
) { }