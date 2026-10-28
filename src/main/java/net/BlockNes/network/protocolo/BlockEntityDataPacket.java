package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class BlockEntityDataPacket extends Paquete {
    private final int x, y, z;
    private final byte[] nbt;

    public BlockEntityDataPacket(int x, int y, int z, byte[] nbt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.nbt = nbt;
    }

    @Override
    public int id() {
        return Info.BLOCK_ENTITY_DATA;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(x).putInt(y).putInt(z).putBytes(nbt);
    }

    public static byte[] blockEntityData(int x, int y, int z, byte[] nbt) {
        return new BlockEntityDataPacket(x, y, z, nbt).bytes();
    }
}
