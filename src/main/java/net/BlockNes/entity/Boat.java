package net.BlockNes.entity;

public class Boat extends Vehicle {
    public static final int NETWORK_ID = 90;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Boat(Object handle) { super(handle); }
}
