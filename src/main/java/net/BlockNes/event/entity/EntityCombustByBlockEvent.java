package net.BlockNes.event.entity;

public interface EntityCombustByBlockEvent extends EntityCombustEvent, EntityEvent {
    int getBlockId();
    int getSeconds();
    void setSeconds(int seconds);
}
