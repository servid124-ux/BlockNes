package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class ContainerOpenPacket extends Paquete {
    public static final int CHEST = 0;
    public static final int WORKBENCH = 1;
    public static final int FURNACE = 2;
    public static final int ENCHANT = 3;
    public static final int BREWING = 4;
    public static final int ANVIL = 5;

    private final int window, type, slots, x, y, z;
    private final long entityId;

    public ContainerOpenPacket(int window, int type, int slots, int x, int y, int z) {
        this(window, type, slots, x, y, z, -1L);
    }

    public ContainerOpenPacket(int window, int type, int slots, int x, int y, int z, long entityId) {
        this.window = window;
        this.type = type;
        this.slots = slots;
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityId = entityId;
    }

    @Override
    public int id() {
        return Info.CONTAINER_OPEN;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putByte(window).putByte(type).putShort(slots).putInt(x).putInt(y).putInt(z).putLong(entityId);
    }

    public static byte[] containerOpen(int window, int type, int slots, int x, int y, int z) {
        return new ContainerOpenPacket(window, type, slots, x, y, z).bytes();
    }
}
