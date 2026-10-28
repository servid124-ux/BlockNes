package net.BlockNes.entity;

public class SnowGolem extends Animal {
    public static final int NETWORK_ID = 21;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public SnowGolem(Object handle) { super(handle); }
}
