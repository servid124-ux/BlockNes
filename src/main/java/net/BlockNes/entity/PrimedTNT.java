package net.BlockNes.entity;

public class PrimedTNT extends Entity implements Explosive {
    public static final int NETWORK_ID = 65;
    @Override public int getNetworkId() { return NETWORK_ID; }
    public PrimedTNT(Object handle) { super(handle); }
}
