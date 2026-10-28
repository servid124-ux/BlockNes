package net.BlockNes.entity;

public class Chicken extends Animal {
    public static final int NETWORK_ID = 10;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Chicken(Object handle) { super(handle); }
}
