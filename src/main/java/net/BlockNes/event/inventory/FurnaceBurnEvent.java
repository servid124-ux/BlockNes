package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.block.BlockEvent;
import net.BlockNes.item.Item;

public interface FurnaceBurnEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    Item getFuel();
    int getBurnTime();
    void setBurnTime(int ticks);
}
