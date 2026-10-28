package net.BlockNes.entity;

public class PigZombie extends Monster {
    public static final int NETWORK_ID = 36;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public PigZombie(Object handle) { super(handle); }
}
