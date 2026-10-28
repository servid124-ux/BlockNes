package net.BlockNes.event.inventory;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
 
public interface InventoryOpenEvent extends InventoryEvent, PlayerEvent, Cancellable {
    String getType();
    int getX();
    int getY();
    int getZ();
}
