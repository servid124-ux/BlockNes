package net.BlockNes.event.player;

public interface PlayerLatencyUpdateEvent extends PlayerEvent {
    int getPing();
    int getPreviousPing();
}
