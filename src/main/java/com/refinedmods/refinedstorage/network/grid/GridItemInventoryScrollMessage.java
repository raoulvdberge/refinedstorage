package com.refinedmods.refinedstorage.network.grid;

import com.refinedmods.refinedstorage.container.GridContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class GridItemInventoryScrollMessage {
    private final int slot;
    private final boolean shift;
    private final boolean up;

    public GridItemInventoryScrollMessage(int slot, boolean shift, boolean up) {
        this.slot = slot;
        this.shift = shift;
        this.up = up;
    }

    public static GridItemInventoryScrollMessage decode(PacketBuffer buf) {
        return new GridItemInventoryScrollMessage(buf.readInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void encode(GridItemInventoryScrollMessage message, PacketBuffer buf) {
        buf.writeInt(message.slot);
        buf.writeBoolean(message.shift);
        buf.writeBoolean(message.up);
    }

    public static void handle(GridItemInventoryScrollMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null && ctx.get().getSender().openContainer instanceof GridContainer) {
                ((GridContainer) ctx.get().getSender().openContainer).getGrid().getItemHandler().onInventoryScroll(ctx.get().getSender(), message.slot, message.shift, message.up);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
