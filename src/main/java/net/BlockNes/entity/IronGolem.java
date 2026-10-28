package net.BlockNes.entity;

public class IronGolem extends Animal {
    public static final int NETWORK_ID = 20;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public IronGolem(Object handle) { super(handle); }
}
