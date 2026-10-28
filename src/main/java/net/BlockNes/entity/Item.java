package net.BlockNes.entity;

public class Item extends Entity {
    public static final int NETWORK_ID = 64;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Item(Object handle) { super(handle); }
}
