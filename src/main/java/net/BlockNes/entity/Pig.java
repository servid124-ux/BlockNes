package net.BlockNes.entity;

public class Pig extends Animal {
    public static final int NETWORK_ID = 12;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Pig(Object handle) { super(handle); }
}
