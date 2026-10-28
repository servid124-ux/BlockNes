package net.BlockNes.plugin;

import net.BlockNes.command.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public final class CommandBuilder {
    private final Plugin plugin;
    private final String name;
    private String description = "", usage = "", permission = "";
    private final List<String> aliases = new ArrayList<>();

    CommandBuilder(Plugin plugin, String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("command: the name is empty");
        this.plugin = plugin;
        this.name = name.trim();
    }

    public CommandBuilder describe(String text) { description = text == null ? "" : text; return this; }

    public CommandBuilder usage(String text) { usage = text == null ? "" : text; return this; }

    public CommandBuilder aliases(String... names) {
        if (names != null) for (String a : names) if (a != null && !a.trim().isEmpty()) aliases.add(a.trim());
        return this;
    }

    public CommandBuilder permission(String node) { permission = node == null ? "" : node; return this; }

    public CommandBuilder opOnly() { permission = "op"; return this; }

    public void run(PluginCommand body) {
        if (body == null) throw new IllegalArgumentException("command: the body is null");
        plugin.command(name, description, usage, permission, body);
        addAliases();
    }

    public void executor(CommandExecutor executor) {
        if (executor == null) throw new IllegalArgumentException("command: the executor is null");
        plugin.command(name, description, usage, permission, executor);
        addAliases();
    }

    private void addAliases() {
        if (!aliases.isEmpty()) plugin.alias(name, aliases.toArray(new String[0]));
    }
}
