package net.BlockNes.entity;

public class Spider extends Monster {
    public static final int NETWORK_ID = 35;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Spider(Object handle) { super(handle); }
}
