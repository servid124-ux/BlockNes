package net.BlockNes.entity;

public class Lightning extends Entity {
    public static final int NETWORK_ID = 93;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Lightning(Object handle) { super(handle); }
}
