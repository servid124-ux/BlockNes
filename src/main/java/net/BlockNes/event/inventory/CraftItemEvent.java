package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
import net.BlockNes.item.Item;

import java.util.List;
 
public interface CraftItemEvent extends PlayerEvent, Cancellable {
    List<Item> getInput();
    Item getResult();
}
