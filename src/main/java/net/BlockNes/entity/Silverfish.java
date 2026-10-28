package net.BlockNes.entity;

public class Silverfish extends Monster {
    public static final int NETWORK_ID = 39;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Silverfish(Object handle) { super(handle); }
}
