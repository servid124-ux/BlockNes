package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import net.BlockNes.item.Item;

public final class AddPlayerPacket extends Paquete {
    private final byte[] uuid;
    private final String username, nametag;
    private final boolean mostrarNombre;
    private final long eid;
    private final float x, y, z, yaw, pitch;
    private final Item hand;
    private final int flags;

    public AddPlayerPacket(byte[] uuid, String username, String nametag, boolean mostrarNombre, long eid,
                           float x, float y, float z, float yaw, float pitch, Item hand, int flags) {
        this.uuid = uuid;
        this.username = username;
        this.nametag = nametag;
        this.mostrarNombre = mostrarNombre;
        this.eid = eid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.hand = hand;
        this.flags = flags;
    }

    @Override
    public int id() {
        return Info.ADD_PLAYER;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putBytes(uuid).putString(username).putLong(eid)
            .putFloat(x).putFloat(y).putFloat(z)
            .putFloat(0).putFloat(0).putFloat(0)
            .putFloat(yaw).putFloat(yaw).putFloat(pitch);
        hand.write(o);
        o.putBytes(Metadatos.jugador(nametag, mostrarNombre, flags));
    }

    public static byte[] addPlayer(byte[] uuid, String name, long eid,
                                   float x, float y, float z, float yaw,
                                   float pitch, Item hand, int flags) {
        return addPlayer(uuid, name, name, true, eid, x, y, z, yaw, pitch, hand, flags);
    }

    public static byte[] addPlayer(byte[] uuid, String name, String nametag, boolean showNameTag, long eid,
                                   float x, float y, float z, float yaw, float pitch, Item hand, int flags) {
        return new AddPlayerPacket(uuid, name, nametag, showNameTag, eid, x, y, z, yaw, pitch, hand, flags).bytes();
    }
}
