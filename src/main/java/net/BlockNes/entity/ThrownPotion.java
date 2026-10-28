package net.BlockNes.entity;

public class ThrownPotion extends Projectile {
    public static final int NETWORK_ID = 86;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public ThrownPotion(Object handle) { super(handle); }
}
