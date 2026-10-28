package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityEffectAddEvent extends EntityEvent, Cancellable {
    int getEffect();
    int getAmplifier();
    int getDuration();
}
