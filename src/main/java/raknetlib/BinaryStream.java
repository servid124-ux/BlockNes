package raknetlib;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BinaryStream {
    private byte[] buf;
    private int len;
    private int pos;

    public BinaryStream() { buf = new byte[256]; }

    public BinaryStream(byte[] data) { this(data, 0, data.length); }

    public BinaryStream(byte[] data, int off, int n) {
        buf = new byte[Math.max(n, 16)];
        System.arraycopy(data, off, buf, 0, n);
        len = n;
    }

    private void ensure(int extra) {
        int need = len + extra;
        if (need > buf.length) buf = Arrays.copyOf(buf, Math.max(need, buf.length * 2));
    }

    public BinaryStream putByte(int v) { ensure(1); buf[len++] = (byte) v; return this; }

    public BinaryStream putShort(int v) { ensure(2); buf[len++] = (byte) (v >> 8); buf[len++] = (byte) v; return this; }

    public BinaryStream putLShort(int v) { ensure(2); buf[len++] = (byte) v; buf[len++] = (byte) (v >> 8); return this; }

    public BinaryStream putInt(int v) {
        ensure(4);
        buf[len++] = (byte) (v >> 24); buf[len++] = (byte) (v >> 16); buf[len++] = (byte) (v >> 8); buf[len++] = (byte) v;
        return this;
    }

    public BinaryStream putLInt(int v) {
        ensure(4);
        buf[len++] = (byte) v; buf[len++] = (byte) (v >> 8); buf[len++] = (byte) (v >> 16); buf[len++] = (byte) (v >> 24);
        return this;
    }

    public BinaryStream putLTriad(int v) {
        ensure(3);
        buf[len++] = (byte) v; buf[len++] = (byte) (v >> 8); buf[len++] = (byte) (v >> 16);
        return this;
    }

    public BinaryStream putLong(long v) { putInt((int) (v >> 32)); putInt((int) v); return this; }

    public BinaryStream putFloat(float f) { return putInt(Float.floatToIntBits(f)); }

    public BinaryStream putDouble(double d) { return putLong(Double.doubleToLongBits(d)); }

    public BinaryStream putBytes(byte[] b) { return putBytes(b, 0, b.length); }

    public BinaryStream putBytes(byte[] b, int off, int n) {
        ensure(n);
        System.arraycopy(b, off, buf, len, n);
        len += n;
        return this;
    }

    public BinaryStream putString(String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        putShort(b.length);
        return putBytes(b);
    }

    public BinaryStream putByteString(byte[] b) { putShort(b.length); return putBytes(b); }

    public BinaryStream putLLong(long v) { putLInt((int) v); putLInt((int) (v >> 32)); return this; }

    public BinaryStream putLFloat(float f) { return putLInt(Float.floatToIntBits(f)); }

    public byte[] getByteString() { return getBytes(getShort()); }

    public int getSignedShort() { int v = getShort(); return v >= 0x8000 ? v - 0x10000 : v; }

    public BinaryStream putAddress(InetAddress a, int port) {
        byte[] ip = (a instanceof Inet4Address) ? a.getAddress() : new byte[]{127, 0, 0, 1};
        putByte(4);
        for (byte b : ip) putByte(~b & 0xff);
        return putShort(port);
    }

    private void need(int n) {
        if (n < 0 || pos + n > len) throw new IndexOutOfBoundsException("underflow pos=" + pos + " need=" + n + " len=" + len);
    }

    public int getByte() { need(1); return buf[pos++] & 0xff; }

    public int getShort() { need(2); int v = ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff); pos += 2; return v; }

    public int getLShort() { need(2); int v = ((buf[pos + 1] & 0xff) << 8) | (buf[pos] & 0xff); pos += 2; return v; }

    public int getInt() {
        need(4);
        int v = ((buf[pos] & 0xff) << 24) | ((buf[pos + 1] & 0xff) << 16) | ((buf[pos + 2] & 0xff) << 8) | (buf[pos + 3] & 0xff);
        pos += 4;
        return v;
    }

    public int getLInt() {
        need(4);
        int v = ((buf[pos + 3] & 0xff) << 24) | ((buf[pos + 2] & 0xff) << 16) | ((buf[pos + 1] & 0xff) << 8) | (buf[pos] & 0xff);
        pos += 4;
        return v;
    }

    public int getLTriad() {
        need(3);
        int v = ((buf[pos + 2] & 0xff) << 16) | ((buf[pos + 1] & 0xff) << 8) | (buf[pos] & 0xff);
        pos += 3;
        return v;
    }

    public long getLong() { long hi = getInt() & 0xffffffffL; long lo = getInt() & 0xffffffffL; return (hi << 32) | lo; }

    public float getFloat() { return Float.intBitsToFloat(getInt()); }

    public double getDouble() { return Double.longBitsToDouble(getLong()); }

    public byte[] getBytes(int n) { need(n); byte[] r = Arrays.copyOfRange(buf, pos, pos + n); pos += n; return r; }

    public byte[] getRemaining() { return getBytes(len - pos); }

    public String getString() { return new String(getBytes(getShort()), StandardCharsets.UTF_8); }

    public InetSocketAddress getAddress() {
        int ver = getByte();
        if (ver != 4) throw new IllegalStateException("solo IPv4 (version=" + ver + ")");
        byte[] ip = new byte[4];
        for (int i = 0; i < 4; i++) ip[i] = (byte) (~getByte() & 0xff);
        int port = getShort();
        try {
            return new InetSocketAddress(InetAddress.getByAddress(ip), port);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public int remaining() { return len - pos; }
    public boolean eof() { return pos >= len; }
    public int position() { return pos; }
    public void position(int p) { pos = p; }
    public int length() { return len; }
    public byte[] toByteArray() { return Arrays.copyOf(buf, len); }

    public BinaryStream skip(int n) { need(n); pos += n; return this; }
    public int getSignedByte() { return (byte) getByte(); }
    public long getUnsignedInt() { return getInt() & 0xffffffffL; }
    public long getUnsignedLInt() { return getLInt() & 0xffffffffL; }
    public int getSignedLShort() { int v = getLShort(); return v >= 0x8000 ? v - 0x10000 : v; }
    public BinaryStream putBoolean(boolean v) { return putByte(v ? 1 : 0); }
    public boolean getBoolean() { return getByte() != 0; }

    public BinaryStream putTriad(int v) {
        ensure(3);
        buf[len++] = (byte) (v >> 16); buf[len++] = (byte) (v >> 8); buf[len++] = (byte) v;
        return this;
    }
    public int getTriad() {
        need(3);
        int v = ((buf[pos] & 0xff) << 16) | ((buf[pos + 1] & 0xff) << 8) | (buf[pos + 2] & 0xff);
        pos += 3;
        return v;
    }

    public long getLLong() { long lo = getLInt() & 0xffffffffL; long hi = getLInt() & 0xffffffffL; return (hi << 32) | lo; }
    public float getLFloat() { return Float.intBitsToFloat(getLInt()); }
    public BinaryStream putLDouble(double d) { return putLLong(Double.doubleToLongBits(d)); }
    public double getLDouble() { return Double.longBitsToDouble(getLLong()); }

    public BinaryStream putUnsignedVarInt(long v) {
        v &= 0xffffffffL;
        while ((v & ~0x7FL) != 0) { putByte((int) ((v & 0x7F) | 0x80)); v >>>= 7; }
        return putByte((int) v);
    }
    public long getUnsignedVarInt() {
        long v = 0; int shift = 0, b;
        do {
            b = getByte();
            v |= (long) (b & 0x7F) << shift;
            shift += 7;
            if (shift > 35) throw new IllegalStateException("varuint32 too big");
        } while ((b & 0x80) != 0);
        return v & 0xffffffffL;
    }
     
    public BinaryStream putVarInt(int v) { return putUnsignedVarInt((v << 1) ^ (v >> 31)); }
    public int getVarInt() { int u = (int) getUnsignedVarInt(); return (u >>> 1) ^ -(u & 1); }
     
    public BinaryStream putUnsignedVarLong(long v) {
        while ((v & ~0x7FL) != 0) { putByte((int) ((v & 0x7F) | 0x80)); v >>>= 7; }
        return putByte((int) v);
    }
    public long getUnsignedVarLong() {
        long v = 0; int shift = 0, b;
        do {
            b = getByte();
            v |= (long) (b & 0x7F) << shift;
            shift += 7;
            if (shift > 70) throw new IllegalStateException("varuint64 too big");
        } while ((b & 0x80) != 0);
        return v;
    }
     
    public BinaryStream putVarLong(long v) { return putUnsignedVarLong((v << 1) ^ (v >> 63)); }
    public long getVarLong() { long u = getUnsignedVarLong(); return (u >>> 1) ^ -(u & 1); }

    public BinaryStream putVarString(String s) { byte[] b = s.getBytes(StandardCharsets.UTF_8); putUnsignedVarInt(b.length); return putBytes(b); }
    public String getVarString() { return new String(getVarBytes(), StandardCharsets.UTF_8); }
     
    public BinaryStream putLString(String s) { byte[] b = s.getBytes(StandardCharsets.UTF_8); putLShort(b.length); return putBytes(b); }
    public String getLString() { return new String(getBytes(getLShort()), StandardCharsets.UTF_8); }
     
    public BinaryStream putVarBytes(byte[] b) { putUnsignedVarInt(b.length); return putBytes(b); }
    public byte[] getVarBytes() {
        long n = getUnsignedVarInt();
        if (n > remaining()) throw new IndexOutOfBoundsException("underflow: length " + n + " > remaining " + remaining());
        return getBytes((int) n);
    }

    public byte[] getNbt(raknetlib.nbt.NbtFormat format) {
        int end = raknetlib.nbt.NbtSkipper.end(buf, pos, len, format);
        return getBytes(end - pos);
    }
     
    public raknetlib.nbt.Nbt.Root getNbtTag(raknetlib.nbt.NbtFormat format) {
        raknetlib.nbt.Nbt.Root r = raknetlib.nbt.Nbt.read(buf, pos, len, format);
        pos = r.end;
        return r;
    }
    public BinaryStream putNbtTag(String name, Object value, raknetlib.nbt.NbtFormat format) {
        return putBytes(raknetlib.nbt.Nbt.write(name, value, format));
    }
     
    public byte[] getNetworkNbt() { return getNbt(raknetlib.nbt.NbtFormat.BEDROCK_NETWORK); }
     
    public byte[] getLittleEndianNbt() { return getNbt(raknetlib.nbt.NbtFormat.BEDROCK_LE); }

    public BinaryStream putUUID(java.util.UUID u) { return putLLong(u.getMostSignificantBits()).putLLong(u.getLeastSignificantBits()); }
    public java.util.UUID getUUID() { long m = getLLong(); long l = getLLong(); return new java.util.UUID(m, l); }

    public BinaryStream putVec2(float x, float z) { return putLFloat(x).putLFloat(z); }
    public float[] getVec2() { return new float[] { getLFloat(), getLFloat() }; }
    public BinaryStream putVec3(float x, float y, float z) { return putLFloat(x).putLFloat(y).putLFloat(z); }
    public float[] getVec3() { return new float[] { getLFloat(), getLFloat(), getLFloat() }; }

    public BinaryStream putBlockPos(int x, int y, int z) { return putVarInt(x).putVarInt(y).putVarInt(z); }
    public int[] getBlockPos() { return new int[] { getVarInt(), getVarInt(), getVarInt() }; }
     
    public BinaryStream putNetworkBlockPos(int x, int y, int z) { return putVarInt(x).putUnsignedVarInt(y).putVarInt(z); }
    public int[] getNetworkBlockPos() { return new int[] { getVarInt(), (int) getUnsignedVarInt(), getVarInt() }; }
     
    public BinaryStream putIntBlockPos(int x, int y, int z) { return putInt(x).putInt(y).putInt(z); }
    public int[] getIntBlockPos() { return new int[] { getInt(), getInt(), getInt() }; }

    public BinaryStream putAddressAny(InetSocketAddress sa) {
        InetAddress a = sa.getAddress();
        if (a instanceof java.net.Inet6Address) {
            putByte(6).putLShort(23).putShort(sa.getPort()).putInt(0).putBytes(a.getAddress()).putInt(
                ((java.net.Inet6Address) a).getScopeId());
            return this;
        }
        return putAddress(a == null ? InetAddress.getLoopbackAddress() : a, sa.getPort());
    }
    public InetSocketAddress getAddressAny() {
        int ver = getByte();
        try {
            if (ver == 4) {
                byte[] ip = new byte[4];
                for (int i = 0; i < 4; i++) ip[i] = (byte) (~getByte() & 0xff);
                return new InetSocketAddress(InetAddress.getByAddress(ip), getShort());
            }
            if (ver == 6) {
                getLShort();                        
                int port = getShort();
                getInt();                           
                byte[] ip = getBytes(16);
                getInt();                           
                return new InetSocketAddress(InetAddress.getByAddress(ip), port);
            }
        } catch (java.net.UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException("unknown address version " + ver);
    }
}
