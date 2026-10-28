package net.BlockNes.network.protocolo;

public final class Info {

    public static final int VERSION = 84;

    private Info() {
        throw new UnsupportedOperationException(
            "Info is a constants-only class and cannot be instantiated"
        );
    }

    public static final int LOGIN                         = 0x01;
    public static final int PLAY_STATUS                   = 0x02;
    public static final int SERVER_TO_CLIENT_HANDSHAKE    = 0x03;
    public static final int CLIENT_TO_SERVER_HANDSHAKE    = 0x04;
    public static final int DISCONNECT                    = 0x05;
    public static final int BATCH                         = 0x06;
    public static final int TEXT                          = 0x07;

    public static final int SET_TIME                      = 0x08;
    public static final int START_GAME                    = 0x09;
    public static final int ADD_PLAYER                    = 0x0A;
    public static final int ADD_ENTITY                    = 0x0B;
    public static final int REMOVE_ENTITY                  = 0x0C;
    public static final int ADD_ITEM_ENTITY                = 0x0D;
    public static final int TAKE_ITEM_ENTITY               = 0x0E;
    public static final int MOVE_ENTITY                   = 0x0F;
    public static final int MOVE_PLAYER                   = 0x10;
    public static final int RIDER_JUMP                     = 0x11;
    public static final int REMOVE_BLOCK                  = 0x12;
    public static final int UPDATE_BLOCK                  = 0x13;
    public static final int ADD_PAINTING                   = 0x14;
    public static final int EXPLODE                        = 0x15;

    public static final int LEVEL_EVENT                   = 0x16;
    public static final int BLOCK_EVENT                   = 0x17;
    public static final int ENTITY_EVENT                  = 0x18;
    public static final int MOB_EFFECT                    = 0x19;
    public static final int UPDATE_ATTRIBUTES             = 0x1A;
    public static final int MOB_EQUIPMENT                 = 0x1B;
    public static final int MOB_ARMOR_EQUIPMENT           = 0x1C;

    public static final int INTERACT                      = 0x1E;
    public static final int USE_ITEM                      = 0x1F;
    public static final int PLAYER_ACTION                  = 0x20;
    public static final int HURT_ARMOR                     = 0x21;
    public static final int SET_ENTITY_DATA                = 0x22;
    public static final int SET_ENTITY_MOTION              = 0x23;
    public static final int SET_ENTITY_LINK                = 0x24;

    public static final int SET_HEALTH                     = 0x25;
    public static final int SET_SPAWN_POSITION             = 0x26;
    public static final int ANIMATE                        = 0x27;
    public static final int RESPAWN                        = 0x28;
    public static final int DROP_ITEM                      = 0x29;

    public static final int CONTAINER_OPEN                 = 0x2A;
    public static final int CONTAINER_CLOSE                = 0x2B;
    public static final int CONTAINER_SET_SLOT             = 0x2C;
    public static final int CONTAINER_SET_DATA             = 0x2D;
    public static final int CONTAINER_SET_CONTENT          = 0x2E;
    public static final int CRAFTING_DATA                  = 0x2F;
    public static final int CRAFTING_EVENT                 = 0x30;

    public static final int ADVENTURE_SETTINGS             = 0x31;
    public static final int BLOCK_ENTITY_DATA              = 0x32;
    public static final int PLAYER_INPUT                   = 0x33;
    public static final int FULL_CHUNK_DATA                = 0x34;
    public static final int SET_DIFFICULTY                 = 0x35;
    public static final int CHANGE_DIMENSION               = 0x36;
    public static final int SET_PLAYER_GAMETYPE            = 0x37;
    public static final int PLAYER_LIST                    = 0x38;

    public static final int TELEMETRY_EVENT                = 0x39;
    public static final int SPAWN_EXPERIENCE_ORB           = 0x3A;
    public static final int CLIENTBOUND_MAP_ITEM_DATA      = 0x3B;
    public static final int MAP_INFO_REQUEST               = 0x3C;
    public static final int REQUEST_CHUNK_RADIUS           = 0x3D;
    public static final int CHUNK_RADIUS_UPDATED           = 0x3E;
    public static final int ITEM_FRAME_DROP_ITEM           = 0x3F;

    public static final int REPLACE_SELECTED_ITEM          = 0x40;
    public static final int ADD_ITEM                       = 0x41;
}
