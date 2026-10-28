package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class AddPaintingPacket extends Paquete {
    private final long eid;
    private final int x, y, z, direction;
    private final String title;

    public AddPaintingPacket(long eid, int x, int y, int z, int direction, String title) {
        this.eid = eid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.title = title;
    }

    @Override
    public int id() {
        return Info.ADD_PAINTING;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putInt(x).putInt(y).putInt(z).putInt(direction).putString(title);
    }
}
