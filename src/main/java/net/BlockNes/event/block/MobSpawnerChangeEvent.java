package net.BlockNes.event.block;

import net.BlockNes.Player;
import net.BlockNes.event.Cancellable;

public interface MobSpawnerChangeEvent extends BlockEvent, Cancellable {
    Player getPlayer();
    int getOldType();
    int getNewType();
    void setNewType(int networkId);
}
