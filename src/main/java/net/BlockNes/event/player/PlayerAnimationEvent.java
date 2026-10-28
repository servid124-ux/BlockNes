package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerAnimationEvent extends PlayerEvent, Cancellable {
    int getAnimationType();
}
