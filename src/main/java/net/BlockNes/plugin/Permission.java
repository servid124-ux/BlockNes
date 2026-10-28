package net.BlockNes.plugin;

import java.util.Locale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Permission {
    private final String name;
    private String description;
    private PermissionDefault def;
    private final Map<String, Boolean> children = new LinkedHashMap<>();

    public Permission(String name) { this(name, "", PermissionDefault.OP); }

    public Permission(String name, String description) { this(name, description, PermissionDefault.OP); }

    public Permission(String name, String description, PermissionDefault def) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("the permission needs a name");
        this.name = name.trim().toLowerCase(Locale.ROOT);
        this.description = description == null ? "" : description;
        this.def = def == null ? PermissionDefault.OP : def;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Permission setDescription(String d) { description = d == null ? "" : d; return this; }
    public PermissionDefault getDefault() { return def; }
    public Permission setDefault(PermissionDefault d) { def = d == null ? PermissionDefault.OP : d; return this; }

    public Map<String, Boolean> getChildren() { return Collections.unmodifiableMap(children); }
    public Permission addChild(String node, boolean value) { children.put(node.trim().toLowerCase(Locale.ROOT), value); return this; }

    @Override public String toString() { return name; }
}
