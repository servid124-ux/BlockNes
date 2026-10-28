package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerGameModeChangeEvent extends PlayerEvent, Cancellable {
    int getOldGamemode();
    int getNewGamemode();
}
