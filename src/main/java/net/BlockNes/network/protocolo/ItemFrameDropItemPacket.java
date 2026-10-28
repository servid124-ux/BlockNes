package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class ItemFrameDropItemPacket extends Paquete {
    private final int x, y, z;
    private final Item dropItem;

    public ItemFrameDropItemPacket(int x, int y, int z, Item dropItem) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dropItem = dropItem;
    }

    public static ItemFrameDropItemPacket leer(BinaryStream in) {
        int z = in.getInt(), y = in.getInt(), x = in.getInt();
        return new ItemFrameDropItemPacket(x, y, z, Item.read(in));
    }

    public int x() {
        return x;
    }
    public int y() {
        return y;
    }
    public int z() {
        return z;
    }
    public Item dropItem() {
        return dropItem;
    }

    @Override
    public int id() {
        return Info.ITEM_FRAME_DROP_ITEM;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(z).putInt(y).putInt(x);
        dropItem.write(o);
    }
}
