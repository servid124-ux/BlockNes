package net.BlockNes.plugin;

import java.util.HashMap;
import java.util.Map;

public final class Cooldowns {
    private final Map<String, Long> until = new HashMap<>();

    public boolean tryUse(String key, long ms) {
        long now = System.currentTimeMillis();
        Long u = until.get(key);
        if (u != null && u > now) return false;
        until.put(key, now + Math.max(0, ms));
        return true;
    }
     
    public long remaining(String key) { Long u = until.get(key); return u == null ? 0 : Math.max(0, u - System.currentTimeMillis()); }
     
    public long remainingSeconds(String key) { return (remaining(key) + 999) / 1000; }
    public boolean isActive(String key) { return remaining(key) > 0; }
    public void reset(String key) { until.remove(key); }
    public void clear() { until.clear(); }
}
