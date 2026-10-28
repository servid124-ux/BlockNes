package net.BlockNes.event.entity;

import java.util.List;
import net.BlockNes.event.Cancellable;

public interface EntityExplodeEvent extends Cancellable {
    int getDimension();
    double getX();
    double getY();
    double getZ();
    double getSize();
    List<int[]> getBlocks();
}
