package net.BlockNes.event.player;

public interface PlayerDeathEvent extends PlayerEvent {
     
    String getDeathMessage();
    void setDeathMessage(String message);
    boolean getKeepInventory();
    void setKeepInventory(boolean keepInventory);
}
