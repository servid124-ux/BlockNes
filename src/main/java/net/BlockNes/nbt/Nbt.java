package net.BlockNes.nbt;

import raknetlib.BinaryStream;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Nbt {
    private Nbt() {}

    public static final int END = 0, BYTE = 1, SHORT = 2, INT = 3, LONG = 4, FLOAT = 5, DOUBLE = 6, BYTE_ARRAY = 7, STRING = 8,
        LIST = 9, COMPOUND = 10, INT_ARRAY = 11;

    public static final class Writer {
        private final BinaryStream o = new BinaryStream();

        public Writer() { o.putByte(COMPOUND).putLShort(0); }

        private Writer head(int type, String name) {
            byte[] n = name.getBytes(StandardCharsets.UTF_8);
            o.putByte(type).putLShort(n.length).putBytes(n);
            return this;
        }

        public Writer string(String name, String v) {
            head(STRING, name);
            byte[] b = v.getBytes(StandardCharsets.UTF_8);
            o.putLShort(b.length).putBytes(b);
            return this;
        }

        public Writer integer(String name, int v) { head(INT, name); o.putLInt(v); return this; }

        public Writer shorted(String name, int v) { head(SHORT, name); o.putLShort(v); return this; }

        public Writer bytev(String name, int v) { head(BYTE, name); o.putByte(v); return this; }
         
        public Writer item(String name, int id, int damage, int count) {
            head(COMPOUND, name);
            shorted("id", id);
            shorted("Damage", damage);
            bytev("Count", count);
            o.putByte(END);
            return this;
        }

        public Writer items(String name, net.BlockNes.item.Item[] items) {
            head(LIST, name);
            int count = 0;
            if (items != null) for (net.BlockNes.item.Item it : items) if (it != null && !it.isAir()) count++;
            o.putByte(COMPOUND).putLInt(count);
            if (items != null) for (int slot = 0; slot < items.length; slot++) {
                net.BlockNes.item.Item it = items[slot];
                if (it == null || it.isAir()) continue;
                shorted("id", it.id);
                shorted("Damage", it.damage);
                bytev("Count", it.count);
                bytev("Slot", slot);
                o.putByte(END);
            }
            return this;
        }

        public byte[] end() { o.putByte(END); return o.toByteArray(); }
    }

    public static Map<String, Object> readRoot(BinaryStream in) {
        int t = in.getByte();
        if (t != COMPOUND) throw new IllegalArgumentException("NBT: la raiz no es un compound (" + t + ")");
        readString(in);
        return readCompound(in, 0);
    }

    private static String readString(BinaryStream in) {
        return new String(in.getBytes(in.getLShort()), StandardCharsets.UTF_8);
    }

    private static Map<String, Object> readCompound(BinaryStream in, int depth) {
        if (depth > 16) throw new IllegalArgumentException("NBT demasiado profundo");
        Map<String, Object> m = new LinkedHashMap<>();
        while (true) {
            int t = in.getByte();
            if (t == END) return m;
            String name = readString(in);
            m.put(name, readValue(in, t, depth));
        }
    }

    private static Object readValue(BinaryStream in, int t, int depth) {
        switch (t) {
            case BYTE: return in.getByte();
            case SHORT: return in.getLShort();
            case INT: return in.getLInt();
            case LONG: { long lo = in.getLInt() & 0xffffffffL; long hi = in.getLInt() & 0xffffffffL; return (hi << 32) | lo; }
            case FLOAT: return (double) Float.intBitsToFloat(in.getLInt());
            case DOUBLE: { long lo = in.getLInt() & 0xffffffffL; long hi = in.getLInt() & 0xffffffffL; return Double.longBitsToDouble((hi << 32) | lo); }
            case BYTE_ARRAY: { int n = in.getLInt(); if (n < 0 || n > in.remaining()) throw new IllegalArgumentException("NBT: invalid array"); return in.getBytes(n); }
            case STRING: return readString(in);
            case LIST: {
                int et = in.getByte(), n = in.getLInt();
                if (n < 0 || n > 65536) throw new IllegalArgumentException("NBT: lista invalida");
                Object[] a = new Object[n];
                for (int i = 0; i < n; i++) a[i] = readValue(in, et, depth + 1);
                return a;
            }
            case COMPOUND: return readCompound(in, depth + 1);
            case INT_ARRAY: {
                int n = in.getLInt();
                if (n < 0 || n > in.remaining() / 4) throw new IllegalArgumentException("NBT: invalid array");
                int[] a = new int[n];
                for (int i = 0; i < n; i++) a[i] = in.getLInt();
                return a;
            }
            default: throw new IllegalArgumentException("NBT: tag desconocido " + t);
        }
    }
}
