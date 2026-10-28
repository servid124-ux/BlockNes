package net.BlockNes.event;

public final class LevelTimings {
    private LevelTimings() {}

    public static TimingsHandler of(String level, String name) { return new TimingsHandler(level + " " + name); }
}
