package com.winccoa.nodejs;

import org.graalvm.collections.Pair;
import org.graalvm.polyglot.Value;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record DpConnectData(
        boolean answer,
        String[] names,
        Value[] values
) {
    public List<Map.Entry<String, Value>> asList() {
        return IntStream.range(0, names.length)
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(names[i], values[i]))
                .collect(Collectors.toList());
    }
}