package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetTimePacket extends Paquete {
    private final int time;
    private final boolean started;

    public SetTimePacket(int time, boolean started) {
        this.time = time;
        this.started = started;
    }

    @Override
    public int id() {
        return Info.SET_TIME;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(time).putByte(started ? 1 : 0);
    }

    public static byte[] setTime(int time, boolean started) {
        return new SetTimePacket(time, started).bytes();
    }
}
