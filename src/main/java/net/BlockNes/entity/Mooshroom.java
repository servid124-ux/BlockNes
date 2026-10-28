package net.BlockNes.entity;

public class Mooshroom extends Animal {
    public static final int NETWORK_ID = 16;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Mooshroom(Object handle) { super(handle); }
}
