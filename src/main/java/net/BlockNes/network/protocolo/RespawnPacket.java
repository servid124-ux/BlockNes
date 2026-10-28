package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class RespawnPacket extends Paquete {
    private final float x, y, z;
    public RespawnPacket(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int id() {
        return Info.RESPAWN;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putFloat(x).putFloat(y).putFloat(z);
    }

    public static byte[] respawn(float x, float y, float z) {
        return new RespawnPacket(x, y, z).bytes();
    }
}
