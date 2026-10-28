package net.BlockNes.entity;

public class Ghast extends FlyingAnimal {
    public static final int NETWORK_ID = 41;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Ghast(Object handle) { super(handle); }
}
