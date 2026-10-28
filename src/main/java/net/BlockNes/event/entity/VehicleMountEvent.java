package net.BlockNes.event.entity;

import net.BlockNes.Player;
import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;

public interface VehicleMountEvent extends PlayerEvent, Cancellable {
    Player getPlayer();
    String getVehicleType();
    long getVehicleEntityId();
}
