package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

import net.BlockNes.item.Item;

public interface EntityDrinkPotionEvent extends EntityEvent, Cancellable {
    Item getPotion();
}
