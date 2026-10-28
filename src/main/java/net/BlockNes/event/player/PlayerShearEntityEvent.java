package net.BlockNes.event.player;

import java.util.List;
import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;
import net.BlockNes.mob.Mob;

public interface PlayerShearEntityEvent extends PlayerEvent, Cancellable {
    Mob getEntity();
    List<Item> getDrops();
}
