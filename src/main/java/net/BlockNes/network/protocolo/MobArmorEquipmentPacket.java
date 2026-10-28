package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class MobArmorEquipmentPacket extends Paquete {
    private final long eid;
    private final Item[] armor;

    public MobArmorEquipmentPacket(long eid, Item[] armor) {
        this.eid = eid;
        this.armor = armor;
    }

    @Override
    public int id() {
        return Info.MOB_ARMOR_EQUIPMENT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid);
        for (int i = 0; i < 4; i++) (armor != null && i < armor.length ? armor[i] : Item.AIR).write(o);
    }

    public static byte[] mobArmorEquipment(long eid, Item[] armor) {
        return new MobArmorEquipmentPacket(eid, armor).bytes();
    }
}
