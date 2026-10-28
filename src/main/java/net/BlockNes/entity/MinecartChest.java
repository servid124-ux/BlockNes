package net.BlockNes.entity;

public class MinecartChest extends Minecart {
    public static final int NETWORK_ID = 98;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public MinecartChest(Object handle) { super(handle); }
}
