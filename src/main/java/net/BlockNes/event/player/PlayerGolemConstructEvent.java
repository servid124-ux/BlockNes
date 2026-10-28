package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

public interface PlayerGolemConstructEvent extends PlayerEvent, Cancellable {
    int getGolemType();
    int getX();
    int getY();
    int getZ();
}
