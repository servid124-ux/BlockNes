package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class TakeItemEntityPacket extends Paquete {
    private final long target, eid;
    public TakeItemEntityPacket(long target, long eid) {
        this.target = target;
        this.eid = eid;
    }

    @Override
    public int id() {
        return Info.TAKE_ITEM_ENTITY;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(target).putLong(eid);
    }

    public static byte[] takeItemEntity(long target, long eid) {
        return new TakeItemEntityPacket(target, eid).bytes();
    }
}
