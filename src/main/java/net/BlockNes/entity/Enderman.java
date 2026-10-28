package net.BlockNes.entity;

public class Enderman extends Monster {
    public static final int NETWORK_ID = 38;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Enderman(Object handle) { super(handle); }
}
