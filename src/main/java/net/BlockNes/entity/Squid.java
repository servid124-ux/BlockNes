package net.BlockNes.entity;

public class Squid extends WaterAnimal implements Ageable {
    public static final int NETWORK_ID = 17;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Squid(Object handle) { super(handle); }
}
