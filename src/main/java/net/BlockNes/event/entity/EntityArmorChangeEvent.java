package net.BlockNes.event.entity;

import net.BlockNes.item.Item;
import net.BlockNes.event.Cancellable;

public interface EntityArmorChangeEvent extends EntityEvent, Cancellable {
    int getSlot();
    Item getOldItem();
    Item getNewItem();
    void setNewItem(Item item);
}
