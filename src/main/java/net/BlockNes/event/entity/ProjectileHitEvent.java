package net.BlockNes.event.entity;

import net.BlockNes.Player;
import net.BlockNes.mob.Mob;

public interface ProjectileHitEvent {
    String getProjectileType();
    Player getShooterPlayer();
    Mob getShooterMob();
    double getX();
    double getY();
    double getZ();
    Player getHitPlayer();
    Mob getHitMob();
     
    int getBlockId();
}
