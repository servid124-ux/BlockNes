package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityDamageByBlockEvent extends EntityEvent, Cancellable {
    int getBlockId();
    String getCause();
    int getDamage();
    void setDamage(int damage);
}
