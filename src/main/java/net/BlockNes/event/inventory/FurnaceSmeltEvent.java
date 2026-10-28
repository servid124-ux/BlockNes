package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.block.BlockEvent;
import net.BlockNes.item.Item;

public interface FurnaceSmeltEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    Item getSource();
    Item getResult();
    void setResult(Item result);
}
