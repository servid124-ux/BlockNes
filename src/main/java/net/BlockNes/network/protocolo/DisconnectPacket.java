package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class DisconnectPacket extends Paquete {
    private final String message;
    public DisconnectPacket(String message) {
        this.message = message;
    }

    @Override
    public int id() {
        return Info.DISCONNECT;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putString(message);
    }

    public static byte[] disconnect(String message) {
        return new DisconnectPacket(message).bytes();
    }
}
