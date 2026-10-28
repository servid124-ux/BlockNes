package net.BlockNes.entity;

public class Creeper extends Monster {
    public static final int NETWORK_ID = 33;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Creeper(Object handle) { super(handle); }
}
