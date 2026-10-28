package net.BlockNes.plugin;

import java.util.Locale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PermissionAttachment {
    private final PluginManager manager;
    private final Plugin plugin;
    private final String player;
    private final Map<String, Boolean> perms = new LinkedHashMap<>();
    private boolean removed;

    PermissionAttachment(PluginManager manager, Plugin plugin, String player) {
        this.manager = manager; this.plugin = plugin; this.player = player;
    }

    public Plugin getPlugin() { return plugin; }

    public String getPlayer() { return player; }

    public Map<String, Boolean> getPermissions() { return Collections.unmodifiableMap(new LinkedHashMap<>(perms)); }

    public PermissionAttachment setPermission(String node, boolean value) {
        perms.put(node.trim().toLowerCase(Locale.ROOT), value);
        return this;
    }

    public PermissionAttachment unsetPermission(String node) {
        perms.remove(node.trim().toLowerCase(Locale.ROOT));
        return this;
    }

    public PermissionAttachment clearPermissions() { perms.clear(); return this; }

    public boolean isRemoved() { return removed; }

    public void remove() {
        if (removed) return;
        removed = true;
        manager.removeAttachment(this);
    }

    Map<String, Boolean> entries() { return perms; }
}
