package net.BlockNes.plugin;

import net.BlockNes.Player;
import net.BlockNes.Server;
import net.BlockNes.lang.Lang;
import net.BlockNes.util.Log;

import java.util.*;
import java.util.function.Function;
import net.BlockNes.network.protocolo.TextPacket;

final class Hud {
    static final int REFRESH = 10, KEEPALIVE = 20, BAR = 24;

    private static final class Line { Plugin owner; String key; int order; String text; Function<Player, String> fn; long until = Long.MAX_VALUE; }
    private static final class Boss { Plugin owner; String key; String text; double progress; }
    private static final class State {
        final Map<String, Line> lines = new LinkedHashMap<>();
        final Map<String, Boss> bosses = new LinkedHashMap<>();
        String action; long actionUntil; String title, sub; long titleUntil;
        String shownPopup = "", shownTip = "", sentTip = ""; long popupAt = -999, tipAt = -999;
         
        HudPosition position; int screenWidth = -1, offsetY = Integer.MIN_VALUE; boolean hidden;
    }

    static final HudPosition DEFAULT_POSITION = HudPosition.RIGHT;
    static final int DEFAULT_OFFSET = 0, DEFAULT_WIDTH = 427;
     
    int maxLines = 15;
    private boolean configured;
    private void configure(Server server) {
        if (configured) return; configured = true;
        maxLines = Math.max(1, Math.min(40, server.getConfigInt("hud-max-lines", 15)));
    }

    private final Map<Long, State> states = new HashMap<>();
    private final Map<String, Line> globals = new LinkedHashMap<>();
    private long now;

    private State of(Player p) { return states.computeIfAbsent(p.eid, k -> new State()); }
    private static String k(Plugin o, String key) { return o.name() + ":" + key; }

    void line(Plugin o, Player p, String key, int order, String text) { line(o, p, key, order, text, 0); }
     
    void line(Plugin o, Player p, String key, int order, String text, long ttl) {
        if (text == null) { removeLine(o, p, key); return; }
        Map<String, Line> m = of(p).lines;
        Line l = m.get(k(o, key));
        if (l == null) { l = new Line(); l.owner = o; l.key = key; m.put(k(o, key), l); }
        l.order = order; l.text = text; l.fn = null;
        l.until = ttl > 0 ? now + ttl : Long.MAX_VALUE;
    }
     
    void setPosition(Player p, HudPosition pos) { of(p).position = pos; }
    HudPosition position(Player p) { State s = states.get(p.eid); return s != null && s.position != null ? s.position : DEFAULT_POSITION; }
    void setHidden(Player p, boolean h) { of(p).hidden = h; }
    boolean hidden(Player p) { State s = states.get(p.eid); return s != null && s.hidden; }
    void setScreenWidth(Player p, int px) { of(p).screenWidth = px <= 0 ? -1 : Math.max(160, Math.min(2000, px)); }
    int screenWidth(Player p) { State s = states.get(p.eid); return s != null && s.screenWidth > 0 ? s.screenWidth : DEFAULT_WIDTH; }
    void setOffsetY(Player p, int n) { of(p).offsetY = Math.max(-20, Math.min(20, n)); }
    int offsetY(Player p) { State s = states.get(p.eid); return s != null && s.offsetY != Integer.MIN_VALUE ? s.offsetY : DEFAULT_OFFSET; }
     
    List<String> linesOf(Player p) {
        State s = states.get(p.eid);
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        for (String t : s.shownTip.split("\n", -1)) if (!t.isEmpty()) out.add(t);
        return out;
    }
    void global(Plugin o, String key, int order, Function<Player, String> fn) {
        Line l = new Line(); l.owner = o; l.key = key; l.order = order; l.fn = fn;
        globals.put(k(o, key), l);
    }
    void removeLine(Plugin o, Player p, String key) { of(p).lines.remove(k(o, key)); }
    void removeGlobal(Plugin o, String key) { globals.remove(k(o, key)); for (State s : states.values()) s.lines.remove(k(o, key)); }
    void clear(Plugin o, Player p) { State s = of(p); s.lines.values().removeIf(l -> l.owner == o); s.bosses.values().removeIf(b -> b.owner == o); }
    void title(Player p, String title, String sub, long stay) { State s = of(p); s.title = title == null ? "" : title; s.sub = sub == null ? "" : sub; s.titleUntil = now + Math.max(1, stay); }
    void actionBar(Player p, String text, long ticks) { State s = of(p); s.action = text; s.actionUntil = now + Math.max(1, ticks); }
    void boss(Plugin o, Player p, String key, String text, double progress) {
        Map<String, Boss> m = of(p).bosses;
        Boss b = m.get(k(o, key));
        if (b == null) { b = new Boss(); b.owner = o; b.key = key; m.put(k(o, key), b); }
        b.text = text == null ? "" : text; b.progress = Math.max(0, Math.min(1, progress));
    }
    void removeBoss(Plugin o, Player p, String key) { of(p).bosses.remove(k(o, key)); }
    String[] view(Player p) { State s = states.get(p.eid); return s == null ? new String[]{"", ""} : new String[]{s.shownPopup, s.shownTip}; }

    void removeAll(Plugin o) {
        globals.values().removeIf(l -> l.owner == o);
        for (State s : states.values()) { s.lines.values().removeIf(l -> l.owner == o); s.bosses.values().removeIf(b -> b.owner == o); }
    }

    static int width(String t) {
        int w = 0; boolean bold = false;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '\u00a7' && i + 1 < t.length()) {
                char f = Character.toLowerCase(t.charAt(++i));
                if (f == 'l') bold = true; else if (f == 'r' || (f >= '0' && f <= '9') || (f >= 'a' && f <= 'f')) bold = false;
                continue;
            }
            int cw;
            switch (c) {
                case 'i': case '!': case '.': case ',': case ':': case ';': case '|': cw = 2; break;
                case '\'': case 'l': case '`': cw = 3; break;
                case ' ': case 'I': case 't': case '[': case ']': case '*': case '(': case ')': case '{': case '}': cw = 4; break;
                case 'f': case 'k': case '<': case '>': case '"': case '@': case '~': cw = 5; break;
                default: cw = 6;
            }
            w += cw + 1 + (bold ? 1 : 0);
        }
        return w;
    }

    static String layout(List<String> lines, HudPosition pos, int screenWidth, int offsetY) {
        if (lines.isEmpty()) return "";
        List<String> out = new ArrayList<>();
        if (pos == HudPosition.CENTER) out.addAll(lines);
        else {
            int maxW = 0;
            for (String l : lines) maxW = Math.max(maxW, width(l));
            int half = screenWidth / 2, margin = 6;
            for (String l : lines) {
                int w = width(l);
                if (pos == HudPosition.RIGHT) {
                    int sx = Math.max(0, half - maxW - margin);            
                    int pre = Math.max(0, (2 * sx + w) / 4);
                    out.add(spaces(pre) + l);
                } else {
                    int sx = Math.max(maxW / 2, half - margin);              
                    int post = Math.max(0, (2 * sx - w) / 4);
                    out.add(l + spaces(post) + "\u00a7r");
                }
            }
        }
        StringBuilder sb = new StringBuilder(String.join("\n", out));
        for (int i = 0; i < offsetY; i++) sb.append("\n ");
        for (int i = 0; i > offsetY; i--) sb.insert(0, " \n");
        return sb.toString();
    }
    private static String spaces(int n) { char[] c = new char[n]; java.util.Arrays.fill(c, ' '); return new String(c); }

    static String bar(double progress) {
        int on = (int) Math.round(progress * BAR);
        StringBuilder sb = new StringBuilder("\u00a7a");
        for (int i = 0; i < BAR; i++) { if (i == on) sb.append("\u00a78"); sb.append('|'); }
        return sb.toString();
    }

    void render(Server server, PluginManager pm, long tick) {
        now = tick; configure(server);
        Set<Long> present = new HashSet<>();
        for (Player p : new ArrayList<>(server.allPlayers())) {
            if (!p.loggedIn || !p.spawned || p.dead) continue;
            present.add(p.eid);
            State s = states.get(p.eid);

            boolean any = s != null || !globals.isEmpty() || pm.hasHandlers(Events.PlayerHud.class);
            if (!any) continue;
            if (s == null) s = of(p);

            s.lines.values().removeIf(l -> l.until <= now);           
            List<Line> ls = new ArrayList<>(globals.values());
            ls.addAll(s.lines.values());
            ls.sort(Comparator.comparingInt(l -> l.order));           
            List<String> lines = new ArrayList<>();
            for (Line l : ls) {
                String t;
                try { t = l.fn != null ? l.fn.apply(p) : l.text; }
                catch (Throwable ex) { Log.warn(net.BlockNes.lang.Lang.t("plg.hud.line.fail", l.owner.name(), l.key, ex)); t = null; }
                if (t != null && !t.isEmpty()) lines.add(t);
            }
            String evPopup = "";
            if (pm.hasHandlers(Events.PlayerHud.class)) {
                Events.PlayerHud ev = pm.fire(new Events.PlayerHud(p, lines));
                evPopup = ev.popup == null ? "" : ev.popup;
            }

            String popup;
            if (s.action != null && s.actionUntil > now) popup = s.action;
            else if (!s.bosses.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Boss b : s.bosses.values()) { if (sb.length() > 0) sb.append('\n'); sb.append(b.text).append('\n').append(bar(b.progress)); }
                popup = sb.toString();
            } else popup = evPopup;

            if (lines.size() > maxLines) lines = new ArrayList<>(lines.subList(0, maxLines));
            boolean titleOn = s.title != null && s.titleUntil > now;
            String tip, sent;
            if (titleOn) { tip = s.sub.isEmpty() ? s.title : s.title + "\n" + s.sub; sent = tip; }           
            else if (s.hidden) { tip = ""; sent = ""; }
            else {
                tip = String.join("\n", lines);
                sent = layout(lines, s.position != null ? s.position : DEFAULT_POSITION, s.screenWidth > 0 ? s.screenWidth : DEFAULT_WIDTH, s.offsetY != Integer.MIN_VALUE ? s.offsetY : DEFAULT_OFFSET);
            }

            if (!popup.equals(s.shownPopup) || (!popup.isEmpty() && now - s.popupAt >= KEEPALIVE)) {
                server.send(p, TextPacket.text(TextPacket.POPUP, "", popup)); s.shownPopup = popup; s.popupAt = now;
            }
            if (!sent.equals(s.sentTip) || (!sent.isEmpty() && now - s.tipAt >= KEEPALIVE)) {
                server.send(p, TextPacket.text(TextPacket.TIP, "", sent)); s.sentTip = sent; s.tipAt = now;
            }
            s.shownTip = tip;                                            
        }
        states.keySet().removeIf(id -> !present.contains(id));           
    }
}
