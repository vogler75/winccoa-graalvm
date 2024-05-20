package com.winccoa;

import org.graalvm.polyglot.Value;

public record DpConnectData(
        String[] name,
        Value[] value
) { }