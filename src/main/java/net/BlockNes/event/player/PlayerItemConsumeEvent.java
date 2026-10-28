package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;
 
public interface PlayerItemConsumeEvent extends PlayerEvent, Cancellable {
    Item getItem();
}
