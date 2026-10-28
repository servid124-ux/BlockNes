package net.BlockNes.entity;

public class Cow extends Animal {
    public static final int NETWORK_ID = 11;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Cow(Object handle) { super(handle); }
}
