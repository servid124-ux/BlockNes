package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityDamageEvent extends EntityEvent, Cancellable {
    int getDamage();
    void setDamage(int damage);
    String getCause();
    Object getDamager();
}
