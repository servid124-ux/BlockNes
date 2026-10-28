package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class AddEntityPacket extends Paquete {
    private final long eid;
    private final int type;
    private final float x, y, z, mx, my, mz, yaw, pitch;
    private final byte[] metadatos;

    public AddEntityPacket(long eid, int type, float x, float y, float z, float mx, float my, float mz,
                           float yaw, float pitch, byte[] metadatos) {
        this.eid = eid;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
        this.yaw = yaw;
        this.pitch = pitch;
        this.metadatos = metadatos;
    }

    @Override
    public int id() {
        return Info.ADD_ENTITY;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putInt(type)
            .putFloat(x).putFloat(y).putFloat(z)
            .putFloat(mx).putFloat(my).putFloat(mz)
            .putFloat(yaw * 0.71111f).putFloat(pitch * 0.71111f)
            .putInt(0).putBytes(metadatos).putShort(0);
    }

    public static byte[] addProjectile(long eid, int type, float x, float y, float z, float mx, float my, float mz,
                                       float yaw, float pitch, int potionId, long shooterEid) {
        return new AddEntityPacket(
            eid, type, x, y, z, mx, my, mz, yaw, pitch,
            Metadatos.proyectil(potionId, shooterEid)
        ).bytes();
    }

    public static byte[] addEntity(long eid, int type, float x, float y, float z, float mx, float my, float mz,
                                   float yaw, float pitch, String nametag) {
        return addEntity(eid, type, x, y, z, mx, my, mz, yaw, pitch, nametag, null);
    }

    public static byte[] addEntity(long eid, int type, float x, float y, float z, float mx, float my, float mz,
                                   float yaw, float pitch, String nametag, int[][] extra) {
        return new AddEntityPacket(eid, type, x, y, z, mx, my, mz, yaw, pitch, Metadatos.mob(nametag, extra)).bytes();
    }
}
