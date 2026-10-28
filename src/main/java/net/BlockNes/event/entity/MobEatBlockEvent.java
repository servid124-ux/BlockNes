package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface MobEatBlockEvent extends EntityEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
    int getBlockMeta();
    int getResidueId();
    int getResidueMeta();
    void setResidue(int id, int meta);
}
