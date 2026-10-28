package net.BlockNes.entity;

public class Villager extends Creature implements NPC, Ageable {
    public static final int NETWORK_ID = 15;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Villager(Object handle) { super(handle); }
}
