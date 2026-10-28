package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityEffectRemoveEvent extends EntityEvent, Cancellable {
    int getEffect();
}
