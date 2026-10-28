package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;

public interface EntityInventoryChangeEvent extends EntityEvent, Cancellable {
    int getSlot();
    Item getOldItem();
    Item getNewItem();
    void setNewItem(Item item);
}
