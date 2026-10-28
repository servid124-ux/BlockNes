package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityBlockChangeEvent extends Cancellable {
    int getX();
    int getY();
    int getZ();
    int getFromId();
    int getToId();
    int getToMeta();
}
