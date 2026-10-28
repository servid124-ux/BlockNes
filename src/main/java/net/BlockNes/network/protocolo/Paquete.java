package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public abstract class Paquete {
     
    public abstract int id();

    protected void escribir(BinaryStream o) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not sent by the server");
    }

    public final byte[] bytes() {
        BinaryStream o = new BinaryStream().putByte(id());
        escribir(o);
        return o.toByteArray();
    }
}
