package com.refinedmods.refinedstorage.network;

import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FluidFilterSlotUpdateMessage {
    private final int containerSlot;
    private final FluidStack stack;

    public FluidFilterSlotUpdateMessage(int containerSlot, FluidStack stack) {
        this.containerSlot = containerSlot;
        this.stack = stack;
    }

    public static void encode(FluidFilterSlotUpdateMessage message, PacketBuffer buf) {
        buf.writeInt(message.containerSlot);
        message.stack.writeToPacket(buf);
    }

    public static FluidFilterSlotUpdateMessage decode(PacketBuffer buf) {
        return new FluidFilterSlotUpdateMessage(buf.readInt(), FluidStack.readFromPacket(buf));
    }

    public static void handle(FluidFilterSlotUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        BaseScreen.executeLater(gui -> {
            if (message.containerSlot >= 0 && message.containerSlot < gui.getContainer().inventorySlots.size()) {
                Slot slot = gui.getContainer().getSlot(message.containerSlot);

                if (slot instanceof FluidFilterSlot) {
                    ((FluidFilterSlot) slot).getFluidInventory().setFluid(slot.getSlotIndex(), message.stack);
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
