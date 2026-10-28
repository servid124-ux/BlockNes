package net.BlockNes.event.entity;

import net.BlockNes.item.Item;

public interface EntityEatItemEvent extends EntityEatEvent {
    Item getFood();
}
