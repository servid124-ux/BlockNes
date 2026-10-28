package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.level.Position;

public interface PlayerTeleportEvent extends PlayerEvent, Cancellable {
    Position getFrom();
    Position getTo();
}
