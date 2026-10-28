package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class UseItemPacket extends Paquete {
    public final int x, y, z, face, slot;
    public final float fx, fy, fz, posX, posY, posZ;
    public final Item item;

    public UseItemPacket(int x, int y, int z, int face, float fx, float fy, float fz,
                         float posX, float posY, float posZ, int slot, Item item) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
        this.fx = fx;
        this.fy = fy;
        this.fz = fz;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.slot = slot;
        this.item = item;
    }

    public static UseItemPacket leer(BinaryStream in) {
        int x = in.getInt(), y = in.getInt(), z = in.getInt();
        int face = in.getByte();
        float fx = in.getFloat(), fy = in.getFloat(), fz = in.getFloat();
        float px = in.getFloat(), py = in.getFloat(), pz = in.getFloat();
        int slot = in.getInt();
        return new UseItemPacket(x, y, z, face, fx, fy, fz, px, py, pz, slot, Item.read(in));
    }

    @Override
    public int id() {
        return Info.USE_ITEM;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(x).putInt(y).putInt(z).putByte(face)
            .putFloat(fx).putFloat(fy).putFloat(fz)
            .putFloat(posX).putFloat(posY).putFloat(posZ)
            .putInt(slot);
        item.write(o);
    }
}
