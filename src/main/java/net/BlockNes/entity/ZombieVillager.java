package net.BlockNes.entity;

public class ZombieVillager extends Zombie {
    public static final int NETWORK_ID = 44;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public ZombieVillager(Object handle) { super(handle); }
}
