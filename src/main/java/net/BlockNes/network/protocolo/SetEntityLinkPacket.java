package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetEntityLinkPacket extends Paquete {
    private final long from, to;
    private final int type;

    public SetEntityLinkPacket(long from, long to, int type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    @Override
    public int id() {
        return Info.SET_ENTITY_LINK;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(from).putLong(to).putByte(type);
    }
}
