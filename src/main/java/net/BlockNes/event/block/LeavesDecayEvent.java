package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface LeavesDecayEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
}
