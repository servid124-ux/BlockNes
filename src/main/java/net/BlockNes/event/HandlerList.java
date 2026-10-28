package net.BlockNes.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HandlerList {
    private static final Map<Class<?>, List<Object>> REGISTRY = new HashMap<>();

    private HandlerList() {}

    public static synchronized List<Object> getRegisteredListeners(Class<?> event) {
        return new ArrayList<>(REGISTRY.getOrDefault(event, Collections.emptyList()));
    }

    public static synchronized void register(Class<?> event, Object listener) {
        REGISTRY.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public static synchronized void unregisterAll(Object listener) {
        for (List<Object> l : REGISTRY.values()) l.remove(listener);
    }

    public static synchronized void unregisterAll() { REGISTRY.clear(); }
}
