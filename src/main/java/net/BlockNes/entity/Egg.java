package net.BlockNes.entity;

public class Egg extends Projectile {
    public static final int NETWORK_ID = 82;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Egg(Object handle) { super(handle); }
}
