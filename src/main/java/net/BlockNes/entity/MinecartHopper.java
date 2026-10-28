package net.BlockNes.entity;

public class MinecartHopper extends Minecart {
    public static final int NETWORK_ID = 96;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public MinecartHopper(Object handle) { super(handle); }
}
