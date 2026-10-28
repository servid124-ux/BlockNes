package net.BlockNes.event.level;

import net.BlockNes.event.Cancellable;

public interface WeatherChangeEvent extends LevelEvent, Cancellable {
    int getType();
    void setType(int type);
    int getTicks();
    void setTicks(int ticks);
}
