package net.BlockNes.event.entity;

public interface EntityEatBlockEvent extends EntityEatEvent {
    int getX();
    int getY();
    int getZ();
    int getBlockId();
    int getBlockMeta();
}
