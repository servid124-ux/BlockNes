package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class AddItemEntityPacket extends Paquete {
    private final long eid;
    private final Item item;
    private final float x, y, z, sx, sy, sz;

    public AddItemEntityPacket(long eid, Item item, float x, float y, float z, float sx, float sy, float sz) {
        this.eid = eid;
        this.item = item;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    @Override
    public int id() {
        return Info.ADD_ITEM_ENTITY;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid);
        item.write(o);
        o.putFloat(x).putFloat(y).putFloat(z).putFloat(sx).putFloat(sy).putFloat(sz);
    }

    public static byte[] addItemEntity(long eid, Item item, float x, float y, float z, float sx, float sy, float sz) {
        return new AddItemEntityPacket(eid, item, x, y, z, sx, sy, sz).bytes();
    }
}
