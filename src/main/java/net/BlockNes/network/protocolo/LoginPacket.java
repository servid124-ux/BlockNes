package net.BlockNes.network.protocolo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import net.BlockNes.players.LoginParser;
import raknetlib.BinaryStream;
import raknetlib.batch.Zlib;

public final class LoginPacket extends Paquete {
     
    private static final Pattern JWT = Pattern.compile("\"([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]*)\"");
    private static final int MAX_BLOQUE = 4 * 1024 * 1024;

    private final int protocolo;
    private final String nombre, uuid, skinId;
    private final byte[] skin;

    private LoginPacket(int protocolo, String nombre, String uuid, String skinId, byte[] skin) {
        this.protocolo = protocolo;
        this.nombre = nombre;
        this.uuid = uuid;
        this.skinId = skinId;
        this.skin = skin;
    }

    public static LoginPacket leer(BinaryStream in, int protocoloEsperado) throws DataFormatException {
        int protocolo = in.getInt();
        if (protocolo != protocoloEsperado) return new LoginPacket(protocolo, null, null, null, null);

        BinaryStream r = new BinaryStream(Zlib.inflate(in.getBytes(in.getInt()), MAX_BLOQUE));
        String cadena = new String(r.getBytes(r.getLInt()), StandardCharsets.UTF_8);
        String skinJwt = new String(r.getBytes(r.getLInt()), StandardCharsets.UTF_8);

        String nombre = null, uuid = null;
        Matcher m = JWT.matcher(cadena);
        while (m.find()) {
            String payload = LoginParser.jwtPayload(m.group(1));
            if (payload == null) continue;
            String dn = LoginParser.jsonString(payload, "displayName"), idn = LoginParser.jsonString(payload, "identity");
            if (dn != null) nombre = dn;
            if (idn != null) uuid = idn;
        }

        String skinId = null;
        byte[] skin = null;
        String skinPayload = LoginParser.jwtPayload(skinJwt);
        if (skinPayload != null) {
            skinId = LoginParser.jsonString(skinPayload, "SkinId");
            String sd = LoginParser.jsonString(skinPayload, "SkinData");
            if (sd != null) {
                try { skin = Base64.getDecoder().decode(sd); } catch (IllegalArgumentException e) { skin = new byte[0]; }
            }
        }
        return new LoginPacket(protocolo, nombre, uuid, skinId, skin);
    }

    public int protocolo() {
        return protocolo;
    }

    public String nombre() {
        return nombre;
    }

    public String uuid() {
        return uuid;
    }

    public String skinId() {
        return skinId;
    }

    public byte[] skin() {
        return skin;
    }

    @Override
    public int id() {
        return Info.LOGIN;
    }
}
