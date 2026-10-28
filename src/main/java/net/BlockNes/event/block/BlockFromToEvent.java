package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface BlockFromToEvent extends BlockEvent, Cancellable {
    int getSourceX();
    int getSourceY();
    int getSourceZ();
    int getX();
    int getY();
    int getZ();
    int getLiquidId();
    int getNewMeta();
}
