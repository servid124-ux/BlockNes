package net.BlockNes.entity;

public class Bat extends FlyingAnimal {
    public static final int NETWORK_ID = 19;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Bat(Object handle) { super(handle); }
}
