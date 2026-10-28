package net.BlockNes.event.server;

public interface QueryRegenerateEvent extends ServerEvent {
    String getMotd();
    int getPlayers();
    int getMaxPlayers();
    String getVersion();
    String getLevel();
}
