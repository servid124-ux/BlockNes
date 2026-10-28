package net.BlockNes.event.server;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
import net.BlockNes.network.protocolo.Paquete;
 
public interface DataPacketReceiveEvent extends ServerEvent, PlayerEvent, Cancellable {
    int getPacketId();
    byte[] getPacket();
}
