package net.BlockNes.event;

public class TimingsHandler {
    private final String name;

    public TimingsHandler(String name) { this.name = name; }

    public String getName() { return name; }

    public void startTiming() {}

    public void stopTiming() {}

    public void reset() {}
}
