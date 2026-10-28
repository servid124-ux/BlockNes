package net.BlockNes.entity;

public class Sheep extends Animal implements Colorable {
    public static final int NETWORK_ID = 13;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Sheep(Object handle) { super(handle); }
}
