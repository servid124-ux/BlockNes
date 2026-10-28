package net.BlockNes.entity;

public class Stray extends Skeleton {
    public static final int NETWORK_ID = 46;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Stray(Object handle) { super(handle); }
}
