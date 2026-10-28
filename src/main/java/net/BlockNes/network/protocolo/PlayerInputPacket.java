package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class PlayerInputPacket extends Paquete {
    private final float motX, motY;
    private final boolean jumping, sneaking;

    public PlayerInputPacket(float motX, float motY, boolean jumping, boolean sneaking) {
        this.motX = motX;
        this.motY = motY;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public static PlayerInputPacket leer(BinaryStream in) {
        float mx = in.getFloat(), my = in.getFloat();
        int flags = in.getByte();
        return new PlayerInputPacket(mx, my, (flags & 0x80) > 0, (flags & 0x40) > 0);
    }

    public float motX() {
        return motX;
    }
    public float motY() {
        return motY;
    }
    public boolean jumping() {
        return jumping;
    }
    public boolean sneaking() {
        return sneaking;
    }

    @Override
    public int id() {
        return Info.PLAYER_INPUT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putFloat(motX).putFloat(motY).putByte((jumping ? 0x80 : 0) | (sneaking ? 0x40 : 0));
    }
}
