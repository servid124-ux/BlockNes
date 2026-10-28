package net.BlockNes.plugin;

public final class Args {
    private Args() {}

    public static boolean has(String[] args, int i) { return args != null && i >= 0 && i < args.length; }

    public static String get(String[] args, int i, String def) { return has(args, i) ? args[i] : def; }

    public static int getInt(String[] args, int i, int def) {
        if (!has(args, i)) return def;
        try { return Integer.parseInt(args[i].trim()); }
        catch (NumberFormatException e) { return def; }
    }

    public static int getInt(String[] args, int i, int def, int min, int max) {
        int v = getInt(args, i, def);
        return v < min || v > max ? def : v;
    }

    public static double getDouble(String[] args, int i, double def) {
        if (!has(args, i)) return def;
        try {
            double v = Double.parseDouble(args[i].trim());
            return Double.isFinite(v) ? v : def;
        } catch (NumberFormatException e) { return def; }
    }

    public static boolean getBoolean(String[] args, int i, boolean def) {
        if (!has(args, i)) return def;
        switch (args[i].trim().toLowerCase(java.util.Locale.ROOT)) {
            case "true": case "on": case "si": case "sí": case "yes": case "1": return true;
            case "false": case "off": case "no": case "0": return false;
            default: return def;
        }
    }

    public static String join(String[] args, int from) {
        if (args == null || from >= args.length) return "";
        return String.join(" ", java.util.Arrays.copyOfRange(args, Math.max(0, from), args.length));
    }
}
