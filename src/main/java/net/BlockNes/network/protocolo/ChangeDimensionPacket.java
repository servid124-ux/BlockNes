package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ChangeDimensionPacket extends Paquete {
    private final int dimension;
    private final float x, y, z;

    public ChangeDimensionPacket(int dimension, float x, float y, float z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int id() {
        return Info.CHANGE_DIMENSION;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(dimension).putFloat(x).putFloat(y).putFloat(z).putByte(0);
    }

    public static byte[] changeDimension(int dimension, float x, float y, float z) {
        return new ChangeDimensionPacket(dimension, x, y, z).bytes();
    }
}
