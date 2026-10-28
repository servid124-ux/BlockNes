package net.BlockNes.entity;

public class Painting extends Hanging {
    public static final int NETWORK_ID = 83;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public Painting(Object handle) { super(handle); }
}
