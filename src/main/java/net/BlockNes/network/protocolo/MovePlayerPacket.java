package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class MovePlayerPacket extends Paquete {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RESET = 1;
    public static final int MODE_TELEPORT = 2;

    private final long eid;
    private final float x, y, z, yaw, headYaw, pitch;
    private final int mode;
    private final boolean onGround;

    public MovePlayerPacket(long eid, float x, float y, float z,
                           float yaw, float headYaw, float pitch,
                           int mode, boolean onGround) {
        this.eid = eid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.pitch = pitch;
        this.mode = mode;
        this.onGround = onGround;
    }

    public static MovePlayerPacket leer(BinaryStream in) {
        long eid = in.getLong();
        float x = in.getFloat(), y = in.getFloat(), z = in.getFloat();
        float yaw = in.getFloat(), bodyYaw = in.getFloat(), pitch = in.getFloat();
        int mode = in.getByte();
        boolean onGround = in.getByte() > 0;
        return new MovePlayerPacket(eid, x, y, z, yaw, bodyYaw, pitch, mode, onGround);
    }

    @Override
    public int id() {
        return Info.MOVE_PLAYER;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putFloat(x).putFloat(y).putFloat(z)
            .putFloat(yaw).putFloat(headYaw).putFloat(pitch)
            .putByte(mode).putByte(onGround ? 1 : 0);
    }

    public static byte[] movePlayer(long eid, float x, float y, float z,
                                    float yaw, float headYaw, float pitch,
                                    int mode, boolean onGround) {
        return new MovePlayerPacket(eid, x, y, z, yaw, headYaw, pitch, mode, onGround).bytes();
    }
}
