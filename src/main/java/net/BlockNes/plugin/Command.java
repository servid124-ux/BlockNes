package net.BlockNes.plugin;

import java.util.Locale;

import net.BlockNes.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Command {
    private final String name;
    private String description, usage, permission = "", permissionMessage;
    private final List<String> aliases = new ArrayList<>();
    Plugin owner;                        

    protected Command(String name) { this(name, "", ""); }

    protected Command(String name, String description, String usage, String... aliases) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("the command needs a name");
        this.name = name.trim().toLowerCase(Locale.ROOT).replaceFirst("^/", "");
        this.description = description == null ? "" : description;
        this.usage = usage == null ? "" : usage;
        setAliases(aliases);
    }

    public abstract boolean execute(CommandSender sender, String label, String[] args);

    public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final Command setDescription(String d) { description = d == null ? "" : d; return this; }
    public final String getUsage() { return usage; }
    public final Command setUsage(String u) { usage = u == null ? "" : u; return this; }
    public final String getPermission() { return permission; }
    public final Command setPermission(String p) { permission = p == null ? "" : p; return this; }
     
    public final String getPermissionMessage() { return permissionMessage; }
    public final Command setPermissionMessage(String m) { permissionMessage = m; return this; }
    public final List<String> getAliases() { return Collections.unmodifiableList(aliases); }
     
    public final Command setAliases(String... names) {
        aliases.clear();
        if (names != null) for (String a : Arrays.asList(names)) {
            String n = a == null ? "" : a.trim().toLowerCase(Locale.ROOT).replaceFirst("^/", "");
            if (!n.isEmpty() && !n.equals(name) && !aliases.contains(n)) aliases.add(n);
        }
        return this;
    }

    public final Plugin getPlugin() { return owner; }

    public final boolean testPermission(CommandSender sender) {
        if (permission.isEmpty()) return true;
        if (permission.equals("op")) return sender.isOp();
        return owner != null ? owner.hasPermission(sender, permission) : sender.isOp();
    }

    @Override public String toString() { return "/" + name; }
}
