package com.refinedmods.refinedstorage.network.grid;

import com.refinedmods.refinedstorage.network.ClientProxy;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class GridCraftingStartResponseMessage {
    public static GridCraftingStartResponseMessage decode(PacketBuffer buf) {
        return new GridCraftingStartResponseMessage();
    }

    public static void encode(GridCraftingStartResponseMessage message, PacketBuffer buf) {
    }

    public static void handle(GridCraftingStartResponseMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.onReceivedCraftingStartResponseMessage());
        ctx.get().setPacketHandled(true);
    }
}