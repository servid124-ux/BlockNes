package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class HurtArmorPacket extends Paquete {
    private final int health;
    public HurtArmorPacket(int health) {
        this.health = health;
    }

    @Override
    public int id() {
        return Info.HURT_ARMOR;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(health);
    }

    public static byte[] hurtArmor(int health) {
        return new HurtArmorPacket(health).bytes();
    }
}
