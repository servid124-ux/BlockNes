package net.BlockNes.plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Permissions {
    private final Map<String, List<String>> players = new HashMap<>();
    private final Map<String, List<String>> groups = new HashMap<>();

    void load(Path file) {
        players.clear(); groups.clear();
        if (!Files.exists(file)) return;    
        Properties p = new Properties();
        try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) { p.load(r); } catch (IOException e) { return; }
        for (String k : p.stringPropertyNames()) {
            List<String> v = new ArrayList<>();
            for (String s : p.getProperty(k).split(",")) if (!s.trim().isEmpty()) v.add(s.trim().toLowerCase(Locale.ROOT));
            if (k.toLowerCase(Locale.ROOT).startsWith("group.")) groups.put(k.substring(6).toLowerCase(Locale.ROOT), v);
            else players.put(k.trim().toLowerCase(Locale.ROOT), v);
        }
    }

    public boolean has(String player, String node) {
        return Boolean.TRUE.equals(lookup(player, node));
    }

    Boolean lookup(String player, String node) {
        node = node.toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        Boolean r = null;
        if (groups.containsKey("default")) r = scan(groups.get("default"), node, seen, null);
        List<String> mine = players.get(player.toLowerCase(Locale.ROOT));
        if (mine != null) r = scan(mine, node, seen, r);
        return r;
    }

    private Boolean scan(List<String> entries, String node, Set<String> seen, Boolean cur) {
        for (String e : entries) {
            if (e.startsWith("@") || (!e.startsWith("-") && groups.containsKey(e) && !e.contains("."))) {
                String g = e.startsWith("@") ? e.substring(1) : e;
                if (seen.add(g) && groups.containsKey(g)) cur = scan(groups.get(g), node, seen, cur);
            } else {
                boolean neg = e.startsWith("-");
                String n = neg ? e.substring(1) : e;
                if (matches(n, node)) cur = !neg;
            }
        }
        return cur;
    }

    private static boolean matches(String pattern, String node) {
        if (pattern.equals("*") || pattern.equals(node)) return true;
        return pattern.endsWith(".*") && node.startsWith(pattern.substring(0, pattern.length() - 1));
    }
}
