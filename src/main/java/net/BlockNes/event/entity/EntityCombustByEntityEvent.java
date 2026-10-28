package net.BlockNes.event.entity;

import net.BlockNes.Player;
import net.BlockNes.mob.Mob;

public interface EntityCombustByEntityEvent extends EntityCombustEvent, EntityEvent {
    Player getCombusterPlayer();
    Mob getCombusterMob();
    int getSeconds();
    void setSeconds(int seconds);
}
