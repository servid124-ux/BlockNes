package net.BlockNes.plugin;

import net.BlockNes.datos.Json;
import net.BlockNes.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonConfig {
    private final Path file;
    private final Map<String, Object> root = new LinkedHashMap<>();

    JsonConfig(Path file) { this.file = file; reload(); }

    public JsonConfig reload() {
        root.clear();
        if (Files.exists(file)) {
            try {
                String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                if (!text.trim().isEmpty()) {
                    Object v = Json.parse(text);
                    if (v instanceof Map) root.putAll(Json.obj(v));
                }
            } catch (Exception e) {
                Log.warn(net.BlockNes.lang.Lang.t("plg.json.invalid", file, e));
            }
        }
        return this;
    }

    public Object get(String path) { return node(path); }
    public Object get(String path, Object def) { Object v = node(path); return v == null ? def : v; }
    public boolean has(String path) { return node(path) != null; }
    public String getString(String path, String def) { Object v = node(path); return v == null ? def : String.valueOf(v); }
    public int getInt(String path, int def) {
        Object v = node(path);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return v == null ? def : Integer.parseInt(String.valueOf(v).trim()); } catch (NumberFormatException e) { return def; }
    }
    public long getLong(String path, long def) {
        Object v = node(path);
        if (v instanceof Number) return ((Number) v).longValue();
        try { return v == null ? def : Long.parseLong(String.valueOf(v).trim()); } catch (NumberFormatException e) { return def; }
    }
    public double getDouble(String path, double def) {
        Object v = node(path);
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return v == null ? def : Double.parseDouble(String.valueOf(v).trim()); } catch (NumberFormatException e) { return def; }
    }
    public boolean getBoolean(String path, boolean def) {
        Object v = node(path);
        if (v instanceof Boolean) return (Boolean) v;
        return v == null ? def : Boolean.parseBoolean(String.valueOf(v).trim());
    }
     
    public List<Object> getList(String path) {
        Object v = node(path);
        return v instanceof List ? new ArrayList<>((List<?>) v) : new ArrayList<>();
    }

    public JsonConfig set(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> cur = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = cur.get(parts[i]);
            if (!(next instanceof Map)) { next = new LinkedHashMap<String, Object>(); cur.put(parts[i], next); }
            @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) next;
            cur = m;
        }
        cur.put(parts[parts.length - 1], value);
        return this;
    }

    public JsonConfig setDefault(String path, Object value) { if (!has(path)) set(path, value); return this; }

    public JsonConfig remove(String path) {
        String[] parts = path.split("\\.");
        Object parent = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (!(parent instanceof Map)) return this;
            parent = ((Map<?, ?>) parent).get(parts[i]);
        }
        if (parent instanceof Map) ((Map<?, ?>) parent).remove(parts[parts.length - 1]);
        return this;
    }

    public List<String> getKeys() { return new ArrayList<>(root.keySet()); }

    public Map<String, Object> getAll() { return new LinkedHashMap<>(root); }

    public void save() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, (toJson(root, 0) + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("could not save " + file + ": " + e, e);
        }
    }

    private Object node(String path) {
        Object cur = root;
        for (String part : path.split("\\.")) {
            if (!(cur instanceof Map)) return null;
            cur = ((Map<?, ?>) cur).get(part);
            if (cur == null) return null;
        }
        return cur;
    }

    static String toJson(Object v, int indent) {
        if (v == null) return "null";
        if (v instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) v;
            if (m.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                sb.append(first ? "\n" : ",\n").append("  ".repeat(indent + 1)).append(quote(String.valueOf(e.getKey()))).append(": ").append(toJson(e.getValue(), indent + 1));
                first = false;
            }
            return sb.append("\n").append("  ".repeat(indent)).append("}").toString();
        }
        if (v instanceof Collection) {
            Collection<?> c = (Collection<?>) v;
            if (c.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object o : c) {
                sb.append(first ? "\n" : ",\n").append("  ".repeat(indent + 1)).append(toJson(o, indent + 1));
                first = false;
            }
            return sb.append("\n").append("  ".repeat(indent)).append("]").toString();
        }
        if (v instanceof Number || v instanceof Boolean) return String.valueOf(v);
        return quote(String.valueOf(v));
    }

    static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
