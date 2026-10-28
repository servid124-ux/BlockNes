package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class TextPacket extends Paquete {
    public static final int RAW = 0;
    public static final int CHAT = 1;
    public static final int TRANSLATION = 2;
    public static final int POPUP = 3;
    public static final int TIP = 4;
    public static final int SYSTEM = 5;

    private final int type;
    private final String source, message;

    public TextPacket(int type, String source, String message) {
        this.type = type;
        this.source = source;
        this.message = message;
    }

    @Override
    public int id() {
        return Info.TEXT;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(type);
        if (type == CHAT || type == POPUP) o.putString(source);
        o.putString(message);
    }

    public static byte[] text(int type, String source, String message) {
        return new TextPacket(type, source, message).bytes();
    }
}
