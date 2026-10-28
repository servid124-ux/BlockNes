package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

import net.BlockNes.item.Item;
 
public interface PlayerItemHeldEvent extends PlayerEvent, Cancellable {
    Item getItem();
    int getSlot();
    int getHotbarSlot();
}
