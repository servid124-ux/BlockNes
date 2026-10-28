package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetPlayerGameTypePacket extends Paquete {
    private final int gamemode;
    public SetPlayerGameTypePacket(int gamemode) {
        this.gamemode = gamemode;
    }

    @Override
    public int id() {
        return Info.SET_PLAYER_GAMETYPE;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(gamemode);
    }

    public static byte[] setPlayerGameType(int gamemode) {
        return new SetPlayerGameTypePacket(gamemode).bytes();
    }
}
