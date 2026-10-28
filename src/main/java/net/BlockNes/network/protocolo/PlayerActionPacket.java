package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class PlayerActionPacket extends Paquete {
    private final long eid;
    private final int action, x, y, z, face;

    public PlayerActionPacket(long eid, int action, int x, int y, int z, int face) {
        this.eid = eid;
        this.action = action;
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    public static PlayerActionPacket leer(BinaryStream in) {
        long eid = in.getLong();
        int action = in.getInt(), x = in.getInt(), y = in.getInt(), z = in.getInt(), face = in.getInt();
        return new PlayerActionPacket(eid, action, x, y, z, face);
    }

    public long eid() {
        return eid;
    }
    public int action() {
        return action;
    }
    public int x() {
        return x;
    }
    public int y() {
        return y;
    }
    public int z() {
        return z;
    }
    public int face() {
        return face;
    }

    @Override
    public int id() {
        return Info.PLAYER_ACTION;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putInt(action).putInt(x).putInt(y).putInt(z).putInt(face);
    }
}
