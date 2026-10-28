package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityGenerateEvent extends EntityEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    int getType();
    int getCause();
}
