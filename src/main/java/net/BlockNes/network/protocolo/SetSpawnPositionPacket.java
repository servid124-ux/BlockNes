package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetSpawnPositionPacket extends Paquete {
    private final int x, y, z;
    public SetSpawnPositionPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int id() {
        return Info.SET_SPAWN_POSITION;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(x).putInt(y).putInt(z);
    }

    public static byte[] setSpawnPosition(int x, int y, int z) {
        return new SetSpawnPositionPacket(x, y, z).bytes();
    }
}
