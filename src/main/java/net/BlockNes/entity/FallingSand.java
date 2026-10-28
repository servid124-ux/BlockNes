package net.BlockNes.entity;

public class FallingSand extends Entity {
    public static final int NETWORK_ID = 66;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public FallingSand(Object handle) { super(handle); }
}
