package net.BlockNes.network.protocolo;

import raknetlib.BinaryStream;

public final class AdventureSettingsPacket extends Paquete {
    private final int flags, userPermission, globalPermission;

    public AdventureSettingsPacket(int flags, int userPermission, int globalPermission) {
        this.flags = flags;
        this.userPermission = userPermission;
        this.globalPermission = globalPermission;
    }

    @Override
    public int id() {
        return Info.ADVENTURE_SETTINGS;
    }

    @Override
    protected void escribir(BinaryStream o) {
        o.putInt(flags).putInt(userPermission).putInt(globalPermission);
    }

    public static byte[] adventureSettings(int flags, int userPermission, int globalPermission) {
        return new AdventureSettingsPacket(flags, userPermission, globalPermission).bytes();
    }
}
