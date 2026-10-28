package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface BlockFormEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getNewBlockId();
}
