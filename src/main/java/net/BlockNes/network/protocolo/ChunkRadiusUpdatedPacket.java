package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ChunkRadiusUpdatedPacket extends Paquete {
    private final int radius;
    public ChunkRadiusUpdatedPacket(int radius) {
        this.radius = radius;
    }

    @Override
    public int id() {
        return Info.CHUNK_RADIUS_UPDATED;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(radius);
    }

    public static byte[] chunkRadiusUpdated(int radius) {
        return new ChunkRadiusUpdatedPacket(radius).bytes();
    }
}
