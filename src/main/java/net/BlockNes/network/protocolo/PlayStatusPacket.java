package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class PlayStatusPacket extends Paquete {
    public static final int LOGIN_SUCCESS = 0;
    public static final int FAILED_CLIENT = 1;
    public static final int FAILED_SERVER = 2;
    public static final int PLAYER_SPAWN = 3;

    private final int status;
    public PlayStatusPacket(int status) {
        this.status = status;
    }

    @Override
    public int id() {
        return Info.PLAY_STATUS;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(status);
    }

    public static byte[] playStatus(int status) {
        return new PlayStatusPacket(status).bytes();
    }
}
