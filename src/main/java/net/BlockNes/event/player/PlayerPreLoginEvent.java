package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerPreLoginEvent extends PlayerEvent, Cancellable {
    String getKickMessage();
    void setKickMessage(String message);
}
