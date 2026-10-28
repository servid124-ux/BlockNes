package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;
import net.BlockNes.mob.Mob;

public interface PlayerMilkCowEvent extends PlayerEvent, Cancellable {
    Mob getCow();
    Item getBucket();
}
