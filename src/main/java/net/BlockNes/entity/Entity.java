package net.BlockNes.entity;

public abstract class Entity {
    protected final Object handle;

    protected Entity(Object handle) { this.handle = handle; }

    public Object getHandle() { return handle; }

    public int getNetworkId() { return -1; }

    public String getName() { return getClass().getSimpleName().replaceAll("(?<=[a-z])(?=[A-Z])", " "); }
}
