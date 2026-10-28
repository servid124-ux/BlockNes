package net.BlockNes.entity;

public class XPOrb extends Entity {
    public static final int NETWORK_ID = 69;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public XPOrb(Object handle) { super(handle); }
}
