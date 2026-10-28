package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

public interface PlayerLoginEvent extends Cancellable {
    String getKickMessage();
    void setKickMessage(String message);
}
