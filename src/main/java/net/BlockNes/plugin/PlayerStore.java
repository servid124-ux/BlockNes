package net.BlockNes.plugin;

import net.BlockNes.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class PlayerStore {
    private final Path folder;
    private final Map<String, JsonConfig> open = new HashMap<>();

    PlayerStore(Path folder) { this.folder = folder; }

    public JsonConfig of(String playerName) {
        String key = key(playerName);
        return open.computeIfAbsent(key, k -> new JsonConfig(folder.resolve(k + ".json")));
    }

    public JsonConfig of(Player p) { return of(p.name); }

    public boolean exists(String playerName) {
        String key = key(playerName);
        return open.containsKey(key) || Files.exists(folder.resolve(key + ".json"));
    }

    public void unload(String playerName) {
        JsonConfig c = open.remove(key(playerName));
        if (c != null) c.save();
    }

    public void saveAll() {
        for (JsonConfig c : open.values()) c.save();
    }

    void close() {
        saveAll();
        open.clear();
    }

    static String key(String name) {
        String n = name == null ? "" : name.trim().toLowerCase(java.util.Locale.ROOT);
        return n.replaceAll("[^a-z0-9_.-]", "_");
    }
}
