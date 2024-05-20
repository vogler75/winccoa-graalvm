package com.winccoa.nodejs;

import org.graalvm.polyglot.Value;

public record DpConnectData(
        String[] name,
        Value[] value
) { }