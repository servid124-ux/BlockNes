package net.BlockNes.event.player;

import net.BlockNes.Player;
import net.BlockNes.event.Cancellable;

import java.util.List;
 
public interface PlayerChatEvent extends PlayerEvent, Cancellable {
    String getMessage();
    void setMessage(String message);
     
    String getFormat();
    void setFormat(String format);
    List<Player> getRecipients();
}
