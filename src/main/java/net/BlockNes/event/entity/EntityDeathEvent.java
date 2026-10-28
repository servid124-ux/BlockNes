package net.BlockNes.event.entity;

import net.BlockNes.item.Item;

import java.util.List;
 
public interface EntityDeathEvent extends EntityEvent {
    List<Item> getDrops();
}
