package net.BlockNes.plugin;

import java.util.Locale;

public enum PermissionDefault {
     
    OP,
     
    NOT_OP,
     
    TRUE,
     
    FALSE;

    public static PermissionDefault parse(String s) {
        if (s == null) return OP;
        switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "notop": case "not_op": case "not-op": return NOT_OP;
            case "true": case "all": return TRUE;
            case "false": case "none": return FALSE;
            default: return OP;
        }
    }
}
