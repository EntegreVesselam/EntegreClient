package com.entegrevesselam.markers;
import java.util.*;
import java.util.function.Supplier;
/** Ownership is runtime object identity, never a visible name or remotely supplied marker ID. */
final class Ownership<T> {
    private final Map<Object,T> layers = new IdentityHashMap<>();
    T open(Object container, T existing, Supplier<T> create) {
        if (existing != null && existing != layers.get(container)) throw new IllegalStateException("ownership_collision");
        T layer = existing == null ? Objects.requireNonNull(create.get()) : existing;
        layers.put(container,layer); return layer;
    }
    static <V> void verifyContents(Map<String,V> actual, Map<String,V> expected) {
        if (actual.size() != expected.size() || expected.entrySet().stream().anyMatch(e -> actual.get(e.getKey()) != e.getValue())) throw new IllegalStateException("ownership_collision");
    }
}
