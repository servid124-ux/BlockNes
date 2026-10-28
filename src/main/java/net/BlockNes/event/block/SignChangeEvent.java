package net.BlockNes.event.block;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
 
public interface SignChangeEvent extends BlockEvent, PlayerEvent, Cancellable {
    int getX();
    int getY();
    int getZ();
    String getLine(int index);
    void setLine(int index, String text);
    String[] getLines();
}
