package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
import net.BlockNes.level.Position;
 
public interface EntityTeleportEvent extends EntityEvent, Cancellable {
    Position getFrom();
    Position getTo();
}
