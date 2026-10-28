package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface CreeperPowerEvent extends EntityEvent, Cancellable {
    int CAUSE_SET_ON = 0, CAUSE_SET_OFF = 1, CAUSE_LIGHTNING = 2;
    int getCause();
}
