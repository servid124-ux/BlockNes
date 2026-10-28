package net.BlockNes.entity;

public class Zombie extends Monster {
    public static final int NETWORK_ID = 32;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Zombie(Object handle) { super(handle); }
}
