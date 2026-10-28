package net.BlockNes.event.level;

public interface SpawnChangeEvent extends LevelEvent {
    String getLevel();
    int getPreviousX();
    int getPreviousY();
    int getPreviousZ();
    int getX();
    int getY();
    int getZ();
}
