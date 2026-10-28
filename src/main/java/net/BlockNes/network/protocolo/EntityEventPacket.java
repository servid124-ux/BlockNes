package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class EntityEventPacket extends Paquete {
    public static final int HURT = 2;
    public static final int DEATH = 3;
    public static final int ATTACK_START = 4;
    public static final int ATTACK_STOP = 5;
    public static final int USE_ITEM = 9;
    public static final int FISH_HOOK_BUBBLE = 11;
    public static final int FISH_HOOK_POSITION = 12;
    public static final int FISH_HOOK_HOOK = 13;
    public static final int FISH_HOOK_TEASE = 14;

    private final long eid;
    private final int event;

    public EntityEventPacket(long eid, int event) {
        this.eid = eid;
        this.event = event;
    }

    @Override
    public int id() {
        return Info.ENTITY_EVENT;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putByte(event);
    }

    public static byte[] entityEvent(long eid, int event) {
        return new EntityEventPacket(eid, event).bytes();
    }
}
