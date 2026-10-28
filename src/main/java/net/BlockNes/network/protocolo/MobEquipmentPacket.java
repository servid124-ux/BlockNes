package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class MobEquipmentPacket extends Paquete {
    private final long eid;
    private final Item item;
    private final int slot, selectedSlot;

    public MobEquipmentPacket(long eid, Item item, int slot, int selectedSlot) {
        this.eid = eid;
        this.item = item;
        this.slot = slot;
        this.selectedSlot = selectedSlot;
    }

    public static MobEquipmentPacket leer(BinaryStream in) {
        long eid = in.getLong();
        Item item = Item.read(in);
        return new MobEquipmentPacket(eid, item, in.getByte(), in.getByte());
    }

    public long eid() {
        return eid;
    }
    public Item item() {
        return item;
    }
    public int slot() {
        return slot;
    }
    public int selectedSlot() {
        return selectedSlot;
    }

    @Override
    public int id() {
        return Info.MOB_EQUIPMENT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid);
        item.write(o);
        o.putByte(slot).putByte(selectedSlot);
    }

    public static byte[] mobEquipment(long eid, Item item, int slot, int selected) {
        return new MobEquipmentPacket(eid, item, slot, selected).bytes();
    }
}
