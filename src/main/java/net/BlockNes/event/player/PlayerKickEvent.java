package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerKickEvent extends PlayerEvent, Cancellable {
    String getReason();
    void setReason(String reason);
}
