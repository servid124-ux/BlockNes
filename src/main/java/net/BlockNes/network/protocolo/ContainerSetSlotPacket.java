package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class ContainerSetSlotPacket extends Paquete {
    private final int window, slot, hotbarSlot;
    private final Item item;

    public ContainerSetSlotPacket(int window, int slot, int hotbarSlot, Item item) {
        this.window = window;
        this.slot = slot;
        this.hotbarSlot = hotbarSlot;
        this.item = item;
    }

    public static ContainerSetSlotPacket leer(BinaryStream in) {
        int window = in.getByte(), slot = in.getShort(), hotbar = in.getShort();
        return new ContainerSetSlotPacket(window, slot, hotbar, Item.read(in));
    }

    public int window() {
        return window;
    }
    public int slot() {
        return slot;
    }
    public int hotbarSlot() {
        return hotbarSlot;
    }
    public Item item() {
        return item;
    }

    @Override
    public int id() {
        return Info.CONTAINER_SET_SLOT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(window).putShort(slot).putShort(hotbarSlot);
        item.write(o);
    }

    public static byte[] containerSlot(int window, int slot, Item item) {
        return new ContainerSetSlotPacket(window, slot, 0, item).bytes();
    }
}
