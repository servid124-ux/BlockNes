package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class MoveEntityPacket extends Paquete {
    private final long eid;
    private final float x, y, z, yaw, pitch, headYaw;

    public MoveEntityPacket(long eid, float x, float y, float z, float yaw, float pitch, float headYaw) {
        this.eid = eid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = headYaw;
    }

    @Override
    public int id() {
        return Info.MOVE_ENTITY;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putFloat(x).putFloat(y).putFloat(z)
            .putByte((int) (pitch / (360.0 / 256)))
            .putByte((int) (yaw / (360.0 / 256)))
            .putByte((int) (headYaw / (360.0 / 256)));
    }

    public static byte[] moveEntity(long eid, float x, float y, float z, float yaw, float pitch, float headYaw) {
        return new MoveEntityPacket(eid, x, y, z, yaw, pitch, headYaw).bytes();
    }
}
