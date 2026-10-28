package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class RemoveBlockPacket extends Paquete {
    private final long eid;
    private final int x, y, z;

    public RemoveBlockPacket(long eid, int x, int y, int z) {
        this.eid = eid;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static RemoveBlockPacket leer(BinaryStream in) {
        long eid = in.getLong();
        int x = in.getInt(), z = in.getInt(), y = in.getByte();
        return new RemoveBlockPacket(eid, x, y, z);
    }

    public long eid() {
        return eid;
    }
    public int x() {
        return x;
    }
    public int y() {
        return y;
    }
    public int z() {
        return z;
    }

    @Override
    public int id() {
        return Info.REMOVE_BLOCK;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putInt(x).putInt(z).putByte(y);
    }
}
