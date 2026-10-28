package net.BlockNes.entity;

public class Blaze extends Monster {
    public static final int NETWORK_ID = 43;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Blaze(Object handle) { super(handle); }
}
