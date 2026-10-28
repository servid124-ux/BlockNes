package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
import net.BlockNes.math.Vector3;

public interface EntityMotionEvent extends EntityEvent, Cancellable {
    Vector3 getVector();
}
