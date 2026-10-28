package net.BlockNes.event.server;

public interface LowMemoryEvent extends ServerEvent {
    long getMemory();
    long getMemoryLimit();
    int getTriggerCount();
}
