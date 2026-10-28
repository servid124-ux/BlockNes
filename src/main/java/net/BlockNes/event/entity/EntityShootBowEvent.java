package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
import net.BlockNes.item.Item;

public interface EntityShootBowEvent extends PlayerEvent, Cancellable {
    Item getBow();
    double getForce();
    void setForce(double force);
}
