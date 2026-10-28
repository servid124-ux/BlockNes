package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ContainerClosePacket extends Paquete {
    private final int window;
    public ContainerClosePacket(int window) {
        this.window = window;
    }

    public static ContainerClosePacket leer(BinaryStream in) {
        return new ContainerClosePacket(in.getByte());
    }
    public int window() {
        return window;
    }

    @Override
    public int id() {
        return Info.CONTAINER_CLOSE;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(window);
    }

    public static byte[] containerClose(int window) {
        return new ContainerClosePacket(window).bytes();
    }
}
