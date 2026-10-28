package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

public interface PlayerPickupExpOrbEvent extends Cancellable {
    double getX();
    double getY();
    double getZ();
    int getExp();
    void setExp(int exp);
}
