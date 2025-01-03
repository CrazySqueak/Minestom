package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientLockDifficultyPacket(boolean locked) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientLockDifficultyPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, ClientLockDifficultyPacket::locked,
            ClientLockDifficultyPacket::new);
}
