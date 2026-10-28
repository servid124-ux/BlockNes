package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;

import net.BlockNes.item.Item;

public interface InventoryPickupArrowEvent extends InventoryEvent, Cancellable {
    Item getItem();
    double getX();
    double getY();
    double getZ();
}
