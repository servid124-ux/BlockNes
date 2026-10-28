package net.BlockNes.entity;

public class ThrownExpBottle extends Projectile {
    public static final int NETWORK_ID = 68;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public ThrownExpBottle(Object handle) { super(handle); }
}
