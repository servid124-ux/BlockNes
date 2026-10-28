package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
 
public interface EntityRegainHealthEvent extends EntityEvent, Cancellable {
    int getAmount();
    void setAmount(int amount);
}
