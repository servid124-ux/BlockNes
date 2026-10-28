package net.BlockNes.plugin;

public abstract class Event extends net.BlockNes.event.Event {
    private boolean cancelled;

    public boolean isCancelled() { return cancelled; }

    public void setCancelled(boolean c) { cancelled = c; }
}
