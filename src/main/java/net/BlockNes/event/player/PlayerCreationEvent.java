package net.BlockNes.event.player;

public interface PlayerCreationEvent {
    String getAddress();
    int getPort();
    String getClientId();
}
