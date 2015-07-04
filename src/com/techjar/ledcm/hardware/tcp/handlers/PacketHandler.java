
package com.techjar.ledcm.hardware.tcp.handlers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.techjar.ledcm.hardware.tcp.Packet;

/**
 *
 * @author Techjar
 */
public abstract class PacketHandler {
    public static BiMap<Packet.ID, Class<PacketHandler>> handlerMap = HashBiMap.create();
    static {
        
    }
}
