package net.BlockNes.event;

public final class Timings {
    private Timings() {}

    public static TimingsHandler of(String name) { return new TimingsHandler(name); }

    public static void reset() {}
}
