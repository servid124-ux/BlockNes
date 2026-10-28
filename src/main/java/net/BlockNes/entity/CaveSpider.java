package net.BlockNes.entity;

public class CaveSpider extends Monster {
    public static final int NETWORK_ID = 40;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public CaveSpider(Object handle) { super(handle); }
}
