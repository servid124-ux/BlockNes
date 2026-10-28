package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerToggleSprintEvent extends PlayerEvent, Cancellable {
    boolean isSprinting();
}
