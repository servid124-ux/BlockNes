package net.BlockNes.entity;

public class Skeleton extends Monster implements ProjectileSource {
    public static final int NETWORK_ID = 34;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Skeleton(Object handle) { super(handle); }
}
