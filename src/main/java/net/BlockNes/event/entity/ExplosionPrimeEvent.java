package net.BlockNes.event.entity;

import net.BlockNes.event.Cancellable;

public interface ExplosionPrimeEvent extends Cancellable {
    double getX();
    double getY();
    double getZ();
    int getFuse();
    void setFuse(int fuse);
}
