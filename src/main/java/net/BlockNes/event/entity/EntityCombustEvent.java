package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityCombustEvent extends EntityEvent, Cancellable {
    int getDuration();
    void setDuration(int seconds);
}
