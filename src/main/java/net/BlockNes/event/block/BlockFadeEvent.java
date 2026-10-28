package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface BlockFadeEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
}
