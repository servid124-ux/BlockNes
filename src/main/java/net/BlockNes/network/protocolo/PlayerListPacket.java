package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public final class PlayerListPacket extends Paquete {
    public static final int ALTA = 0;
    public static final int BAJA = 1;

    public static final class Entrada {
        final byte[] uuid; final long eid; final String nombre, skinId; final byte[] skin;
        public Entrada(byte[] uuid, long eid, String nombre, String skinId, byte[] skin) {
            this.uuid = uuid;
            this.eid = eid;
            this.nombre = nombre;
            this.skinId = skinId;
            this.skin = skin;
        }
    }

    private final int tipo;
    private final List<Entrada> altas;
    private final List<byte[]> bajas;

    private PlayerListPacket(int tipo, List<Entrada> altas, List<byte[]> bajas) {
        this.tipo = tipo;
        this.altas = altas;
        this.bajas = bajas;
    }

    public static PlayerListPacket alta(List<Entrada> entradas) {
        return new PlayerListPacket(ALTA, entradas, null);
    }
    public static PlayerListPacket baja(List<byte[]> uuids) {
        return new PlayerListPacket(BAJA, null, uuids);
    }
    public static PlayerListPacket baja(byte[] uuid) {
        return baja(Collections.singletonList(uuid));
    }

    @Override
    public int id() {
        return Info.PLAYER_LIST;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(tipo);
        if (tipo == ALTA) {
            o.putInt(altas.size());
            for (Entrada e : altas) o.putBytes(e.uuid).putLong(e.eid).putString(e.nombre).putString(e.skinId).putByteString(e.skin);
        } else {
            o.putInt(bajas.size());
            for (byte[] uuid : bajas) o.putBytes(uuid);
        }
    }

    public static byte[] playerListAdd(List<byte[]> uuids, List<Long> eids, List<String> names,
                                       List<String> skinIds, List<byte[]> skins) {
        List<PlayerListPacket.Entrada> entradas = new ArrayList<>();
        for (int i = 0; i < uuids.size(); i++) {
            entradas.add(new PlayerListPacket.Entrada(uuids.get(i), eids.get(i), names.get(i), skinIds.get(i), skins.get(i)));
        }
        return PlayerListPacket.alta(entradas).bytes();
    }

    public static byte[] playerListRemove(byte[] uuid) {
        return PlayerListPacket.baja(uuid).bytes();
    }
}
