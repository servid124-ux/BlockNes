package net.BlockNes.plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class Config extends Properties {
    private final Path file;

    Config(Path file) {
        this.file = file;
        if (Files.exists(file)) try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) { load(r); }
        catch (IOException | RuntimeException e) { net.BlockNes.util.Log.warn(net.BlockNes.lang.Lang.t("cfg.read.fail", file, e)); }
    }

    public String getString(String key, String def) { String v = getProperty(key); return v == null ? def : v; }
    public int getInt(String key, int def) { try { return Integer.parseInt(getProperty(key, "").trim()); } catch (NumberFormatException e) { return def; } }
    public long getLong(String key, long def) { try { return Long.parseLong(getProperty(key, "").trim()); } catch (NumberFormatException e) { return def; } }
    public double getDouble(String key, double def) { try { return Double.parseDouble(getProperty(key, "").trim()); } catch (NumberFormatException e) { return def; } }
    public boolean getBoolean(String key, boolean def) { String v = getProperty(key); return v == null ? def : v.trim().equalsIgnoreCase("true") || v.trim().equals("1") || v.trim().equalsIgnoreCase("si"); }
    public boolean has(String key) { return getProperty(key) != null; }
     
    public boolean exists(String key) { return has(key); }
     
    public Config setDefault(String key, Object value) { if (!has(key)) set(key, value); return this; }
     
    public java.util.List<String> getStringList(String key) {
        java.util.List<String> out = new java.util.ArrayList<>();
        String v = getProperty(key);
        if (v != null) for (String s : v.split(",")) if (!s.trim().isEmpty()) out.add(s.trim());
        return out;
    }
    public Config setList(String key, java.util.Collection<?> values) {
        StringBuilder sb = new StringBuilder();
        for (Object o : values) sb.append(sb.length() == 0 ? "" : ",").append(o);
        return set(key, sb);
    }
     
    public java.util.List<String> keys(String prefix) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String k : stringPropertyNames()) if (k.startsWith(prefix)) out.add(k);
        java.util.Collections.sort(out);
        return out;
    }
     
    public java.util.List<String> getKeys() { return keys(""); }
     
    public java.util.Map<String, String> getAll() {
        java.util.Map<String, String> out = new java.util.TreeMap<>();
        for (String k : stringPropertyNames()) out.put(k, getProperty(k));
        return out;
    }
     
    public Config remove(String key) { super.remove(key); return this; }
    public Config set(String key, Object value) { setProperty(key, String.valueOf(value)); return this; }

    public void save() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) { store(w, null); }
        } catch (IOException e) { throw new RuntimeException("could not save " + file + ": " + e, e); }
    }
}
