package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class StartGamePacket extends Paquete {
    private final int seed, dimension, generator, gamemode;
    private final long eid;
    private final int spawnX, spawnY, spawnZ;
    private final float x, y, z;

    public StartGamePacket(int seed, int dimension, int generator, int gamemode, long eid,
                           int spawnX, int spawnY, int spawnZ, float x, float y, float z) {
        this.seed = seed;
        this.dimension = dimension;
        this.generator = generator;
        this.gamemode = gamemode;
        this.eid = eid;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int id() {
        return Info.START_GAME;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(seed).putByte(dimension).putInt(generator).putInt(gamemode).putLong(eid)
            .putInt(spawnX).putInt(spawnY).putInt(spawnZ)
            .putFloat(x).putFloat(y).putFloat(z)
            .putByte(1).putByte(1).putByte(0)    
            .putString("");                      
    }

    public static byte[] startGame(int seed, int dimension, int generator, int gamemode, long eid,
                                   int spawnX, int spawnY, int spawnZ, float x, float y, float z) {
        return new StartGamePacket(seed, dimension, generator, gamemode, eid, spawnX, spawnY, spawnZ, x, y, z).bytes();
    }
}
