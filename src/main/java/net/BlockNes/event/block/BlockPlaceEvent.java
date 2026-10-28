package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
 
public interface BlockPlaceEvent extends BlockEvent, PlayerEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
    int getBlockMeta();
}
