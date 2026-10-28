package net.BlockNes.entity;

public class Arrow extends Projectile {
    public static final int NETWORK_ID = 80;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Arrow(Object handle) { super(handle); }
}
