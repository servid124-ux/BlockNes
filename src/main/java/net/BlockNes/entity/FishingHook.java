package net.BlockNes.entity;

public class FishingHook extends Projectile {
    public static final int NETWORK_ID = 77;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public FishingHook(Object handle) { super(handle); }
}
