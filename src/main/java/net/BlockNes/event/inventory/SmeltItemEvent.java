package net.BlockNes.event.inventory;

import net.BlockNes.item.Item;

public interface SmeltItemEvent extends FurnaceSmeltEvent {
    Item getSource();
    Item getResult();
}
