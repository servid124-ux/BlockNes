package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;

public interface PlayerGlassBottleEvent extends PlayerEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
    Item getItem();
}
