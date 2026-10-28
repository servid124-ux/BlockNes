package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class DropItemPacket extends Paquete {
    private final int type;
    private final Item item;

    public DropItemPacket(int type, Item item) {
        this.type = type;
        this.item = item;
    }

    public static DropItemPacket leer(BinaryStream in) {
        int type = in.getByte();
        return new DropItemPacket(type, Item.read(in));
    }

    public int type() {
        return type;
    }
    public Item item() {
        return item;
    }

    @Override
    public int id() {
        return Info.DROP_ITEM;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(type);
        item.write(o);
    }
}
