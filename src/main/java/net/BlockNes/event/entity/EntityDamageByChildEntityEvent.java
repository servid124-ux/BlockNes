package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface EntityDamageByChildEntityEvent extends EntityDamageByEntityEvent, Cancellable {
     
    String getChildType();
    int getDamage();
    void setDamage(int damage);
}
