package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityTransformEvent extends EntityEvent, Cancellable {
     
    default int getTargetType() { return -1; }
}
