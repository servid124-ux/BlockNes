package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class LevelEventPacket extends Paquete {
    private final int eventId, data;
    private final float x, y, z;

    public LevelEventPacket(int eventId, float x, float y, float z, int data) {
        this.eventId = eventId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public int id() {
        return Info.LEVEL_EVENT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putShort(eventId).putFloat(x).putFloat(y).putFloat(z).putInt(data);
    }

    public static byte[] levelEvent(int eventId, float x, float y, float z, int data) {
        return new LevelEventPacket(eventId, x, y, z, data).bytes();
    }

    public static final int EVENT_SOUND_BLOCK_PLACE = 1052;

    public static final int EVENT_SOUND_EXP_PICKUP = 1051;

    public static final int EVENT_SOUND_FIZZ = 1004;

    public static final int EVENT_SOUND_TNT = 1005;

    public static final int EVENT_SOUND_ANVIL_FALL = 1022;

    public static final int EVENT_SOUND_EXPLODE = 3501;

    public static final int EVENT_PARTICLE_DESTROY = 2001;
}
