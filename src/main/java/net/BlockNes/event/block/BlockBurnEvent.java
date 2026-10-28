package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface BlockBurnEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
}
