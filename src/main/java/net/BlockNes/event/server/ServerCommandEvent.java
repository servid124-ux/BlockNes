package net.BlockNes.event.server;

import net.BlockNes.command.CommandSender;
import net.BlockNes.event.Cancellable;
 
public interface ServerCommandEvent extends ServerEvent, Cancellable {
    CommandSender getSender();
    String getCommand();
    void setCommand(String command);
}
