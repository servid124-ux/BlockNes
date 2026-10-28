package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;
import net.BlockNes.math.Vector3;
 
public interface PlayerInteractEvent extends PlayerEvent, Cancellable {
    Item getItem();
     
    Vector3 getBlock();
    int getFace();
    boolean isAir();
}
