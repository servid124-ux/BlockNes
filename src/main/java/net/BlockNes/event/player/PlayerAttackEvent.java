package net.BlockNes.event.player;

import net.BlockNes.Player;
import net.BlockNes.item.Item;
import net.BlockNes.mob.Mob;
import net.BlockNes.event.Cancellable;

public interface PlayerAttackEvent extends PlayerEvent, Cancellable {
    long getTargetEntityId();
     
    Player getTargetPlayer();
     
    Mob getTargetMob();
    Item getWeapon();
    double getDamage();
    void setDamage(double damage);
}
