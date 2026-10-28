package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;
 
public interface PlayerDropItemEvent extends PlayerEvent, Cancellable {
    Item getItem();
}
