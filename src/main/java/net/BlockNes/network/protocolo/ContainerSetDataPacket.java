package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ContainerSetDataPacket extends Paquete {
    private final int window, property, value;

    public ContainerSetDataPacket(int window, int property, int value) {
        this.window = window;
        this.property = property;
        this.value = value;
    }

    @Override
    public int id() {
        return Info.CONTAINER_SET_DATA;
    }
    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(window).putShort(property).putShort(value);
    }

    public static byte[] containerSetData(int window, int property, int value) {
        return new ContainerSetDataPacket(window, property, value).bytes();
    }
}
