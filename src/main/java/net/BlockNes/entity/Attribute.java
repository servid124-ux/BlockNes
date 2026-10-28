package net.BlockNes.entity;

public class Attribute {
    private final String name;
    private final float min, max;
    private final float defaultValue;
    private float value;

    public Attribute(String name, float min, float max, float value) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = value;
        this.defaultValue = value;
    }

    public String getName() { return name; }

    public float getMinValue() { return min; }

    public float getMaxValue() { return max; }

    public float getDefaultValue() { return defaultValue; }

    public float getValue() { return value; }

    public void setValue(float value) { this.value = Math.max(min, Math.min(max, value)); }
}
