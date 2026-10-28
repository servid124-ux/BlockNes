package net.BlockNes.plugin;

import net.BlockNes.command.Commands;

public interface PluginCommand {
    void run(Commands.Sender sender, String[] args);
}
