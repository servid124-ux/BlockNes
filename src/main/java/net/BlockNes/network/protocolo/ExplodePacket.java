package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ExplodePacket extends Paquete {
    private final float x, y, z, radius;
    public ExplodePacket(float x, float y, float z, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    @Override
    public int id() {
        return Info.EXPLODE;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putFloat(x).putFloat(y).putFloat(z).putFloat(radius).putInt(0);
    }

    public static byte[] explode(float x, float y, float z, float radius) {
        return new ExplodePacket(x, y, z, radius).bytes();
    }
}
