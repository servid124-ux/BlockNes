package net.BlockNes.entity;

public class Wolf extends Animal {
    public static final int NETWORK_ID = 14;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Wolf(Object handle) { super(handle); }
}
