package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class RemoveEntityPacket extends Paquete {
    private final long eid;
    public RemoveEntityPacket(long eid) {
        this.eid = eid;
    }

    @Override
    public int id() {
        return Info.REMOVE_ENTITY;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid);
    }

    public static byte[] removeEntity(long eid) {
        return new RemoveEntityPacket(eid).bytes();
    }
}
