package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class UpdateBlockPacket extends Paquete {
    private final int x, y, z, id, meta, flags;

    public UpdateBlockPacket(int x, int y, int z, int id, int meta, int flags) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.meta = meta;
        this.flags = flags;
    }

    @Override
    public int id() {
        return Info.UPDATE_BLOCK;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(x).putInt(z).putByte(y).putByte(id).putByte((flags << 4) | (meta & 0x0f));
    }

    public static byte[] updateBlock(int x, int y, int z, int id, int meta, int flags) {
        return new UpdateBlockPacket(x, y, z, id, meta, flags).bytes();
    }

    public static final int FLAG_ALL_PRIORITY = 0b1011;
}
