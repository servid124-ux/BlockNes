package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;

public interface BlockGrowEvent extends BlockEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
    int getBlockMeta();
    int getNewBlockId();
    int getNewBlockMeta();
    void setNewBlock(int id, int meta);
}
