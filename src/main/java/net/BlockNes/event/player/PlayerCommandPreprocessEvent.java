package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;
 
public interface PlayerCommandPreprocessEvent extends PlayerEvent, Cancellable {
     
    String getMessage();
}
