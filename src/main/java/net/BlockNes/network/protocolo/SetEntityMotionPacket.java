package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetEntityMotionPacket extends Paquete {
    private final long eid;
    private final float mx, my, mz;

    public SetEntityMotionPacket(long eid, float mx, float my, float mz) {
        this.eid = eid;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
    }

    @Override
    public int id() {
        return Info.SET_ENTITY_MOTION;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putFloat(mx).putFloat(my).putFloat(mz);
    }

    public static byte[] entityMotion(long eid, float mx, float my, float mz) {
        return new SetEntityMotionPacket(eid, mx, my, mz).bytes();
    }
}
