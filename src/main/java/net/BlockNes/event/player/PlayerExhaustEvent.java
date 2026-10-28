package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerExhaustEvent extends PlayerEvent, Cancellable {
    float getAmount();
    void setAmount(float amount);
}
