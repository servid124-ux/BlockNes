package net.BlockNes.entity;

public class Ocelot extends Animal {
    public static final int NETWORK_ID = 22;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Ocelot(Object handle) { super(handle); }
}
