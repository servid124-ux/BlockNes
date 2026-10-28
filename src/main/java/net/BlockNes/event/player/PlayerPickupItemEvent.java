package net.BlockNes.event.player;

import net.BlockNes.item.Item;
import net.BlockNes.event.Cancellable;

public interface PlayerPickupItemEvent extends PlayerEvent, Cancellable {
    Item getItem();
}
