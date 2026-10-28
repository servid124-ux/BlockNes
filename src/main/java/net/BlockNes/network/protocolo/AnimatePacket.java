package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class AnimatePacket extends Paquete {
    public static final int SWING = 1;
    public static final int WAKE_UP = 3;

    private final int action;
    private final long eid;

    public AnimatePacket(int action, long eid) {
        this.action = action;
        this.eid = eid;
    }

    public static AnimatePacket leer(BinaryStream in) {
        int action = in.getByte();
        return new AnimatePacket(action, in.getLong());
    }

    public int action() {
        return action;
    }
    public long eid() {
        return eid;
    }

    @Override
    public int id() {
        return Info.ANIMATE;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(action).putLong(eid);
    }

    public static byte[] animate(int action, long eid) {
        return new AnimatePacket(action, eid).bytes();
    }
}
