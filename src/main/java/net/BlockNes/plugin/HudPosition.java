package net.BlockNes.plugin;

import java.util.Locale;

public enum HudPosition {
    CENTER, RIGHT, LEFT;

    public static HudPosition parse(String s) {
        if (s == null) return null;
        switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "center": case "centre": case "centro": case "c": case "middle": return CENTER;
            case "right": case "derecha": case "r": case "d": return RIGHT;
            case "left": case "izquierda": case "l": case "i": return LEFT;
            default: return null;
        }
    }
}
