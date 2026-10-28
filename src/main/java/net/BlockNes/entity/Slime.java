package net.BlockNes.entity;

public class Slime extends Living {
    public static final int NETWORK_ID = 37;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Slime(Object handle) { super(handle); }
}
