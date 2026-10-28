package net.BlockNes.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}
