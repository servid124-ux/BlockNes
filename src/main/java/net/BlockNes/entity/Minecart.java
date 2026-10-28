package net.BlockNes.entity;

public class Minecart extends Vehicle {
    public static final int NETWORK_ID = 84;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Minecart(Object handle) { super(handle); }
}
