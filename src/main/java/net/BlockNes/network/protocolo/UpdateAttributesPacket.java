package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;
import java.util.List;

public final class UpdateAttributesPacket extends Paquete {
    public static final class Atributo {
        final String nombre; final float min, max, valor;
        public Atributo(String nombre, float min, float max, float valor) {
            this.nombre = nombre;
            this.min = min;
            this.max = max;
            this.valor = valor;
        }
    }

    private final long eid;
    private final List<Atributo> atributos;

    public UpdateAttributesPacket(long eid, List<Atributo> atributos) {
        this.eid = eid;
        this.atributos = atributos;
    }

    @Override
    public int id() {
        return Info.UPDATE_ATTRIBUTES;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putLong(eid).putShort(atributos.size());
        for (Atributo a : atributos) o.putFloat(a.min).putFloat(a.max).putFloat(a.valor).putString(a.nombre);
    }

    public static byte[] absorptionAttribute(float value) {
        return attributes(0, new UpdateAttributesPacket.Atributo(
            "generic.absorption", 0, Float.MAX_VALUE, Math.max(0f, value)
        ));
    }

    public static byte[] movementSpeedAttribute(float value) {
        return attributes(0, new UpdateAttributesPacket.Atributo(
            "generic.movementSpeed", 0, Float.MAX_VALUE, Math.max(0f, value)
        ));
    }

    public static byte[] playerAttributes(long eid, float food, float saturation, float exhaustion) {
        return attributes(eid,
            new UpdateAttributesPacket.Atributo("player.hunger", 0, 20, food),
            new UpdateAttributesPacket.Atributo("player.saturation", 0, 20, saturation),
            new UpdateAttributesPacket.Atributo("player.exhaustion", 0, 5, exhaustion));
    }

    public static byte[] experienceAttributes(long eid, int level, float progress) {
        return attributes(eid,
            new UpdateAttributesPacket.Atributo("player.level", 0, 2147483647f, level),
            new UpdateAttributesPacket.Atributo("player.experience", 0, 1, progress));
    }

    public static byte[] healthAttribute(int health) {
        return attributes(0, new UpdateAttributesPacket.Atributo("generic.health", 0, 20, health));
    }

    private static byte[] attributes(long eid, UpdateAttributesPacket.Atributo... list) {
        return new UpdateAttributesPacket(eid, java.util.Arrays.asList(list)).bytes();
    }
}
