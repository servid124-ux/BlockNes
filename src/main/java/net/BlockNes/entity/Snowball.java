package net.BlockNes.entity;

public class Snowball extends Projectile {
    public static final int NETWORK_ID = 81;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Snowball(Object handle) { super(handle); }
}
