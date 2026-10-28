package net.BlockNes.event.block;

import net.BlockNes.Player;
import net.BlockNes.event.Cancellable;
import net.BlockNes.item.Item;

public interface ItemFrameRotateEvent extends BlockEvent, Cancellable {
    Player getPlayer();
    Item getItem();
    int getOldRotation();
    int getNewRotation();
    void setNewRotation(int rotation);
}
