package com.refinedmods.refinedstorage.network;

import com.refinedmods.refinedstorage.RS;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SplitPacketMessage {
    /**
     * Internal communication id. Used to indicate to what wrapped message this belongs to.
     */
    private int communicationId;

    /**
     * The index of the split message in the wrapped message.
     */
    private int packetIndex;

    /**
     * The payload.
     */
    private final byte[] payload;

    public SplitPacketMessage(final int communicationId, final int packetIndex, final byte[] payload) {
        this.communicationId = communicationId;
        this.packetIndex = packetIndex;
        this.payload = payload;
    }

    public static void encode(SplitPacketMessage message, PacketBuffer buf) {
        buf.writeVarInt(message.communicationId);
        buf.writeVarInt(message.packetIndex);
        buf.writeByteArray(message.payload);
    }

    public static SplitPacketMessage decode(final PacketBuffer buf) {
        return new SplitPacketMessage(buf.readVarInt(), buf.readVarInt(), buf.readByteArray());
    }

    public static boolean handle(SplitPacketMessage data, Supplier<NetworkEvent.Context> ctx) {
        RS.NETWORK_HANDLER.addPackagePart(data.communicationId, data.packetIndex, data.payload);
        ctx.get().setPacketHandled(true);
        return true;
    }
}
