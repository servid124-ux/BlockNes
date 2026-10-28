package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class FullChunkDataPacket extends Paquete {
    private final int chunkX, chunkZ;
    private final byte[] data;

    public FullChunkDataPacket(int chunkX, int chunkZ, byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.data = data;
    }

    @Override
    public int id() {
        return Info.FULL_CHUNK_DATA;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(chunkX).putInt(chunkZ).putByte(1).putInt(data.length).putBytes(data);
    }

    public static byte[] fullChunkData(int cx, int cz, byte[] data) {
        return new FullChunkDataPacket(cx, cz, data).bytes();
    }
}
