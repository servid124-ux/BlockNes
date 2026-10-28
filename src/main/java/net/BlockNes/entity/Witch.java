package net.BlockNes.entity;

public class Witch extends Monster {
    public static final int NETWORK_ID = 45;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Witch(Object handle) { super(handle); }
}
