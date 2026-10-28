package net.BlockNes.event.server;

import net.BlockNes.event.Cancellable;

public interface RemoteServerCommandEvent extends Cancellable {
    String getLine();
    void setLine(String line);
}
