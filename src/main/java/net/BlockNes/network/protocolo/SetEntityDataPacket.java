package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetEntityDataPacket extends Paquete {
    private final long eid;
    private final byte[] metadatos;

    public SetEntityDataPacket(long eid, byte[] metadatos) {
        this.eid = eid;
        this.metadatos = metadatos;
    }

    @Override
    public int id() {
        return Info.SET_ENTITY_DATA;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putBytes(metadatos);
    }

    public static byte[] clearLocalLeadHolder() {
        return new SetEntityDataPacket(0, Metadatos.localLeadHolderDetached()).bytes();
    }

    public static byte[] setEntityNameTag(long eid, String nametag, boolean showNameTag) {
        return new SetEntityDataPacket(eid, Metadatos.nombre(nametag, showNameTag)).bytes();
    }

    public static byte[] setEntityFlags(long eid, int flags) {
        return new SetEntityDataPacket(eid, Metadatos.flags(flags)).bytes();
    }

    public static byte[] playerAir(long eid, int air) {
        return new SetEntityDataPacket(eid, Metadatos.aireJugador(air)).bytes();
    }

    public static byte[] creeperState(long eid, int swellDirection, int swell, int swellOld, boolean powered) {
        return new SetEntityDataPacket(eid, Metadatos.creeper(swellDirection, swell, swellOld, powered)).bytes();
    }

    public static byte[] entityData(long eid) {
        return entityData(eid, 300);
    }

    public static byte[] entityData(long eid, int air) {
        return new SetEntityDataPacket(eid, Metadatos.entidad(air)).bytes();
    }

    public static byte[] entityData(long eid, int air, int flags) {
        return new SetEntityDataPacket(eid, Metadatos.entidad(air, flags)).bytes();
    }

    public static byte[] setEntityAim(long eid, int flags, long target) {
        return new SetEntityDataPacket(eid, Metadatos.apuntar(flags, target)).bytes();
    }

    public static byte[] setEntityByte(long eid, int key, int value) {
        return new SetEntityDataPacket(eid, Metadatos.unByte(key, value)).bytes();
    }

    public static byte[] setSleeping(long eid, boolean sleeping, int bx, int by, int bz) {
        return new SetEntityDataPacket(eid, Metadatos.durmiendo(sleeping, bx, by, bz)).bytes();
    }
}
