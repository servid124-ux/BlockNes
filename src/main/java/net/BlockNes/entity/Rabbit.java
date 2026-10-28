package net.BlockNes.entity;

public class Rabbit extends Animal {
    public static final int NETWORK_ID = 18;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Rabbit(Object handle) { super(handle); }
}
