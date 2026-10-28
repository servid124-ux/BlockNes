package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class RequestChunkRadiusPacket extends Paquete {
    private final int radius;
    public RequestChunkRadiusPacket(int radius) {
        this.radius = radius;
    }

    public static RequestChunkRadiusPacket leer(BinaryStream in) {
        return new RequestChunkRadiusPacket(in.getInt());
    }
    public int radius() {
        return radius;
    }

    @Override
    public int id() {
        return Info.REQUEST_CHUNK_RADIUS;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(radius);
    }
}
