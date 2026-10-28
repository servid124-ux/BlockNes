package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class MobEffectPacket extends Paquete {
    public static final int ADD = 1;
    public static final int MODIFY = 2;
    public static final int REMOVE = 3;

    private final long eid;
    private final int eventId, effectId, amplifier, duration;
    private final boolean particles;

    public MobEffectPacket(long eid, int eventId, int effectId, int amplifier, boolean particles, int duration) {
        this.eid = eid;
        this.eventId = eventId;
        this.effectId = effectId;
        this.amplifier = amplifier;
        this.particles = particles;
        this.duration = duration;
    }

    @Override
    public int id() {
        return Info.MOB_EFFECT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid)
            .putByte(eventId)
            .putByte(effectId)
            .putByte(amplifier)
            .putByte(particles ? 1 : 0)
            .putInt(duration);
    }

    public static byte[] mobEffect(long eid, int eventId, int effectId,
                                   int amplifier, boolean particles, int duration) {
        return new MobEffectPacket(eid, eventId, effectId, amplifier, particles, duration).bytes();
    }
}
