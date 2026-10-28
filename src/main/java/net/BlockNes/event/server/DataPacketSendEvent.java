package net.BlockNes.event.server;

import net.BlockNes.event.Cancellable;
import net.BlockNes.event.player.PlayerEvent;
import net.BlockNes.network.protocolo.Paquete;
 
public interface DataPacketSendEvent extends ServerEvent, PlayerEvent, Cancellable {
    byte[] getPacket();
}
