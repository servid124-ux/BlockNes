package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class BlockEventPacket extends Paquete {
    private final int x, y, z, case1, case2;

    public BlockEventPacket(int x, int y, int z, int case1, int case2) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.case1 = case1;
        this.case2 = case2;
    }

    @Override
    public int id() {
        return Info.BLOCK_EVENT;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(x).putInt(y).putInt(z).putInt(case1).putInt(case2);
    }

    public static byte[] blockEvent(int x, int y, int z, int case1, int case2) {
        return new BlockEventPacket(x, y, z, case1, case2).bytes();
    }
}
