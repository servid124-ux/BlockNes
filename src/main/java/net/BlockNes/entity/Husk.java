package net.BlockNes.entity;

public class Husk extends Zombie {
    public static final int NETWORK_ID = 47;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Husk(Object handle) { super(handle); }
}
