package net.BlockNes.entity;

public class MinecartTNT extends Minecart {
    public static final int NETWORK_ID = 97;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public MinecartTNT(Object handle) { super(handle); }
}
