package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class SetDifficultyPacket extends Paquete {
    private final int difficulty;
    public SetDifficultyPacket(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public int id() {
        return Info.SET_DIFFICULTY;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(difficulty);
    }

    public static byte[] setDifficulty(int difficulty) {
        return new SetDifficultyPacket(difficulty).bytes();
    }
}
