package net.BlockNes.event.player;

import net.BlockNes.event.Cancellable;

public interface PlayerAchievementAwardedEvent extends PlayerEvent, Cancellable {
    String getAchievement();
}
