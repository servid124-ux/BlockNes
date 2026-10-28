package net.BlockNes.event.player;

import net.BlockNes.item.Item;

public interface PlayerBucketEmptyEvent extends PlayerBucketEvent {
    int getX();
    int getY();
    int getZ();
    int getFace();
     
    int getLiquidId();
    Item getBucket();
    Item getItem();
    void setItem(Item item);
}
