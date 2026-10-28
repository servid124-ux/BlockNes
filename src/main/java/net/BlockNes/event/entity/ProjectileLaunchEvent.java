package net.BlockNes.event.entity;

import net.BlockNes.Player;
import net.BlockNes.event.Cancellable;
import net.BlockNes.mob.Mob;

public interface ProjectileLaunchEvent extends Cancellable {
    String getProjectileType();
     
    Player getShooterPlayer();
     
    Mob getShooterMob();
    double getX();
    double getY();
    double getZ();
}
