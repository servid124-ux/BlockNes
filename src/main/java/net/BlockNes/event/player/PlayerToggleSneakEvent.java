package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerToggleSneakEvent extends PlayerEvent, Cancellable {
    boolean isSneaking();
}
