package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import java.nio.charset.StandardCharsets;

public final class Metadatos {
    public static final int BYTE = 0;
    public static final int SHORT = 1;
    public static final int INT = 2;
    public static final int STRING = 4;
    public static final int POS = 6;
    public static final int LONG = 7;
    public static final int FLAGS = 0, AIR = 1, NAMETAG = 2, SHOW_NAMETAG = 3, SILENT = 4, NO_AI = 15,
        POTION_ID = 16, PLAYER_FLAGS = 16, SHOOTER_ID = 17, TARGET_EID = 17, BED_POSITION = 17,
        LEAD_HOLDER = 23, LEAD = 24;
     
    public static final int BLOCK_INFO = 20;

    private final BinaryStream o = new BinaryStream();

    private Metadatos() {}

    private static Metadatos nuevo() {
        return new Metadatos();
    }

    public Metadatos bytes(int clave, int v) {
        o.putByte((BYTE << 5) | clave).putByte(v);
        return this;
    }
    public Metadatos shorts(int clave, int v) {
        o.putByte((SHORT << 5) | clave).putLShort(v);
        return this;
    }
    public Metadatos ints(int clave, int v) {
        o.putByte((INT << 5) | clave).putLInt(v);
        return this;
    }
    public Metadatos largo(int clave, long v) {
        o.putByte((LONG << 5) | clave).putLLong(v);
        return this;
    }
    public Metadatos texto(int clave, String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        o.putByte((STRING << 5) | clave).putLShort(b.length).putBytes(b);
        return this;
    }
    public Metadatos pos(int clave, int x, int y, int z) {
        o.putByte((POS << 5) | clave).putLInt(x).putLInt(y).putLInt(z);
        return this;
    }
     
    public Metadatos propiedad(int tipo, int clave, int valor) {
        if (tipo == BYTE) return bytes(clave, valor);
        if (tipo == SHORT) return shorts(clave, valor);
        return ints(clave, valor);
    }

    public byte[] fin() {
        return o.putByte(0x7f).toByteArray();
    }

    public static byte[] jugador(String nametag, boolean mostrarNombre, int flags) {
        return nuevo().bytes(FLAGS, flags).shorts(AIR, 300).texto(NAMETAG, nametag)
            .bytes(SHOW_NAMETAG, mostrarNombre ? 1 : 0).bytes(SILENT, 0).bytes(NO_AI, 0)
            .largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).bytes(PLAYER_FLAGS, 0).pos(BED_POSITION, 0, 0, 0).fin();
    }

    public static byte[] entidad(int air) {
        return entidad(air, 0);
    }

    public static byte[] entidad(int air, int flags) {
        return nuevo().bytes(FLAGS, flags).shorts(AIR, air).texto(NAMETAG, "").bytes(SHOW_NAMETAG, 1)
            .bytes(SILENT, 0).bytes(NO_AI, 0).largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).fin();
    }

    public static byte[] bloqueCayendo(int variante) {
        return nuevo().bytes(FLAGS, 0).shorts(AIR, 300).texto(NAMETAG, "").bytes(SHOW_NAMETAG, 1)
            .bytes(SILENT, 0).bytes(NO_AI, 0).ints(BLOCK_INFO, variante).largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).fin();
    }

    public static byte[] bote(int woodId) {
        return mob("", new int[][]{{20, BYTE, woodId & 0xff}});
    }

    public static byte[] mob(String nametag, int[][] extra) {
        int flags = 0;                                    
        if (extra != null) for (int[] e : extra) if (e[0] == FLAGS && e[1] == BYTE) flags = e[2];
        Metadatos m = nuevo().bytes(FLAGS, flags).shorts(AIR, 300).texto(NAMETAG, nametag)
            .bytes(SHOW_NAMETAG, 1).bytes(SILENT, 0).bytes(NO_AI, 0);
        if (extra != null) for (int[] e : extra) if (!(e[0] == FLAGS && e[1] == BYTE)) m.propiedad(e[1], e[0], e[2]);
        return m.largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).fin();
    }

    public static byte[] orbe() {
        return nuevo().bytes(FLAGS, 0).shorts(AIR, 300).texto(NAMETAG, "")
            .bytes(SHOW_NAMETAG, 1).bytes(SILENT, 0).bytes(NO_AI, 1).largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).fin();
    }

    public static byte[] proyectil(int potionId, long shooterEid) {
        Metadatos m = nuevo().bytes(FLAGS, 0).shorts(AIR, 300).texto(NAMETAG, "")
            .bytes(SHOW_NAMETAG, 1).bytes(SILENT, 0).bytes(NO_AI, 0);
        if (potionId >= 0) m.shorts(POTION_ID, potionId);
        if (shooterEid >= 0) m.largo(SHOOTER_ID, shooterEid);
        return m.largo(LEAD_HOLDER, -1L).bytes(LEAD, 0).fin();
    }

    public static byte[] localLeadHolderDetached() {
        return nuevo().largo(LEAD_HOLDER, -1L).fin();
    }

    public static byte[] nombre(String nametag, boolean mostrar) {
        return nuevo().texto(NAMETAG, nametag).bytes(SHOW_NAMETAG, mostrar ? 1 : 0).fin();
    }

    public static byte[] flags(int flags) {
        return nuevo().bytes(FLAGS, flags).fin();
    }

    public static byte[] apuntar(int flags, long target) {
        return nuevo().bytes(FLAGS, flags).largo(TARGET_EID, target).fin();
    }

    public static byte[] unByte(int clave, int valor) {
        return nuevo().bytes(clave, valor).fin();
    }

    public static byte[] creeper(int swellDirection, int swell, int swellOld, boolean powered) {
        return nuevo().bytes(16, swellDirection & 0xff).bytes(17, swell & 0xff).bytes(18, swellOld & 0xff)
            .bytes(19, powered ? 1 : 0).fin();
    }

    public static byte[] durmiendo(boolean sleeping, int bx, int by, int bz) {
        return nuevo().bytes(PLAYER_FLAGS, sleeping ? 1 << 1 : 0)
            .pos(BED_POSITION, sleeping ? bx : 0, sleeping ? by : 0, sleeping ? bz : 0).fin();
    }

    public static byte[] aireJugador(int air) {
        return nuevo().shorts(AIR, Math.max(0, Math.min(300, air))).fin();
    }

    public static byte[] playerMetadata(String nametag, int flags) {
        return Metadatos.jugador(nametag, true, flags);
    }

    public static byte[] playerMetadata(String nametag, boolean showNameTag, int flags) {
        return Metadatos.jugador(nametag, showNameTag, flags);
    }

    public static byte[] mobMetadata(String nametag) {
        return mobMetadata(nametag, null);
    }

    public static byte[] mobMetadata(String nametag, int[][] extra) {
        return Metadatos.mob(nametag, extra);
    }

    public static byte[] projectileMetadata(int potionId, long shooterEid) {
        return Metadatos.proyectil(potionId, shooterEid);
    }
}
