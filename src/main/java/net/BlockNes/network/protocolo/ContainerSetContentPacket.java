package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;
import java.util.List;

public final class ContainerSetContentPacket extends Paquete {
    public static final int WINDOW_ARMOR = 0x78;
    public static final int WINDOW_CREATIVE = 0x79;

    private final int windowId;
    private final Item[] items;

    public ContainerSetContentPacket(int windowId, Item[] items) {
        this.windowId = windowId;
        this.items = items;
    }

    @Override
    public int id() {
        return Info.CONTAINER_SET_CONTENT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(windowId).putShort(items.length);
        for (Item it : items) it.write(o);
        o.putShort(0);
    }

    public static byte[] creativeContent(List<Item> items) {
        return new ContainerSetContentPacket(WINDOW_CREATIVE, items.toArray(new Item[0])).bytes();
    }

    public static byte[] containerContent(int window, Item[] items) {
        return new ContainerSetContentPacket(window, items).bytes();
    }
}
