package net.BlockNes.entity;

public class Effect {
    private final int id;
    private int amplifier;
    private int duration;
    private boolean visible = true;

    public Effect(int id, int amplifier, int duration) {
        this.id = id;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public int getId() { return id; }

    public int getAmplifier() { return amplifier; }

    public void setAmplifier(int amplifier) { this.amplifier = amplifier; }

    public int getDuration() { return duration; }

    public void setDuration(int duration) { this.duration = duration; }

    public boolean isVisible() { return visible; }

    public void setVisible(boolean visible) { this.visible = visible; }
}
