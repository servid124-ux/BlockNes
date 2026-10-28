package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetHealthPacket extends Paquete {
    private final int health;
    public SetHealthPacket(int health) {
        this.health = health;
    }

    @Override
    public int id() {
        return Info.SET_HEALTH;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(health);
    }

    public static byte[] setHealth(int health) {
        return new SetHealthPacket(health).bytes();
    }
}
