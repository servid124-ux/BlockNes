package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;

import net.BlockNes.item.Item;

public interface InventoryPickupItemEvent extends InventoryEvent, Cancellable {
    Item getItem();
}
