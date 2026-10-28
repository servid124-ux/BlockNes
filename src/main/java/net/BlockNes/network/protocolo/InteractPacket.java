package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class InteractPacket extends Paquete {
    private final int action;
    private final long target;

    public InteractPacket(int action, long target) {
        this.action = action;
        this.target = target;
    }

    public static InteractPacket leer(BinaryStream in) {
        int action = in.getByte();
        return new InteractPacket(action, in.getLong());
    }

    public int action() {
        return action;
    }
    public long target() {
        return target;
    }

    @Override
    public int id() {
        return Info.INTERACT;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(action).putLong(target);
    }
}
