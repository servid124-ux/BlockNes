package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

public interface PlayerTextPreSendEvent extends PlayerEvent, Cancellable {
    String getMessage();
    void setMessage(String message);
}
