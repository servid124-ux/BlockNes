package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
import net.BlockNes.level.World;

public interface EntityLevelChangeEvent extends EntityEvent, Cancellable {
    World getOrigin();
    World getTarget();
}
