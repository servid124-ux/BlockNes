package net.BlockNes.plugin;

import java.util.Locale;

public final class Text {
    private Text() {}
    public static final char SECTION = '\u00a7';

    public static String color(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == '&') { sb.append('&'); i++; continue; }
                char l = Character.toLowerCase(n);
                if ((l >= '0' && l <= '9') || (l >= 'a' && l <= 'f') || (l >= 'k' && l <= 'o') || l == 'r') { sb.append(SECTION).append(l); i++; continue; }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String strip(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SECTION && i + 1 < s.length()) { i++; continue; }
            sb.append(c);
        }
        return sb.toString();
    }

    public static int width(String s) { return Hud.width(s == null ? "" : s); }

    public static String bar(double progress, int length, String onColor, String offColor) {
        int len = Math.max(1, length);
        int on = (int) Math.round(Math.max(0, Math.min(1, progress)) * len);
        StringBuilder sb = new StringBuilder(color(onColor));
        for (int i = 0; i < len; i++) { if (i == on) sb.append(color(offColor)); sb.append('|'); }
        return sb.toString();
    }

    public static String ticks(long ticks) { return seconds(Math.max(0, ticks) / 20); }

    public static String seconds(long secs) {
        secs = Math.max(0, secs);
        long h = secs / 3600, m = secs / 60 % 60, s = secs % 60;
        return h > 0 ? String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s) : String.format(Locale.ROOT, "%d:%02d", m, s);
    }

    public static String number(long n) { return String.format(Locale.ROOT, "%,d", n); }

    public static String truncate(String s, int max) {
        if (s == null) return "";
        if (strip(s).length() <= max) return s;
        StringBuilder sb = new StringBuilder(); int vis = 0;
        for (int i = 0; i < s.length() && vis < Math.max(0, max - 3); i++) {
            char c = s.charAt(i);
            if (c == SECTION && i + 1 < s.length()) { sb.append(c).append(s.charAt(++i)); continue; }
            sb.append(c); vis++;
        }
        return sb.append("...").toString();
    }
}
