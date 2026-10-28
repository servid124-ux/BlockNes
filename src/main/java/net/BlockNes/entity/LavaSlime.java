package net.BlockNes.entity;

public class LavaSlime extends Living {
    public static final int NETWORK_ID = 42;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public LavaSlime(Object handle) { super(handle); }
}
