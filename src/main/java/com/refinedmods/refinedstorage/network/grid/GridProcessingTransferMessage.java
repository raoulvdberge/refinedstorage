package com.refinedmods.refinedstorage.network.grid;

import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.inventory.fluid.FluidInventory;
import com.refinedmods.refinedstorage.inventory.item.BaseItemHandler;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GridProcessingTransferMessage {
    private final Collection<ItemStack> inputs;
    private final Collection<ItemStack> outputs;
    private final Collection<FluidStack> fluidInputs;
    private final Collection<FluidStack> fluidOutputs;

    public GridProcessingTransferMessage(Collection<ItemStack> inputs, Collection<ItemStack> outputs, Collection<FluidStack> fluidInputs, Collection<FluidStack> fluidOutputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.fluidInputs = fluidInputs;
        this.fluidOutputs = fluidOutputs;
    }

    public static GridProcessingTransferMessage decode(PacketBuffer buf) {
        int size = buf.readInt();

        List<ItemStack> inputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            inputs.add(StackUtils.readItemStack(buf));
        }

        size = buf.readInt();

        List<ItemStack> outputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            outputs.add(StackUtils.readItemStack(buf));
        }

        size = buf.readInt();

        List<FluidStack> fluidInputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            fluidInputs.add(FluidStack.readFromPacket(buf));
        }

        size = buf.readInt();

        List<FluidStack> fluidOutputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            fluidOutputs.add(FluidStack.readFromPacket(buf));
        }

        return new GridProcessingTransferMessage(inputs, outputs, fluidInputs, fluidOutputs);
    }

    public static void encode(GridProcessingTransferMessage message, PacketBuffer buf) {
        buf.writeInt(message.inputs.size());

        for (ItemStack stack : message.inputs) {
            StackUtils.writeItemStack(buf, stack);
        }

        buf.writeInt(message.outputs.size());

        for (ItemStack stack : message.outputs) {
            StackUtils.writeItemStack(buf, stack);
        }

        buf.writeInt(message.fluidInputs.size());

        for (FluidStack stack : message.fluidInputs) {
            stack.writeToPacket(buf);
        }

        buf.writeInt(message.fluidOutputs.size());

        for (FluidStack stack : message.fluidOutputs) {
            stack.writeToPacket(buf);
        }
    }

    public static void handle(GridProcessingTransferMessage message, Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player != null) {
            ctx.get().enqueueWork(() -> {
                if (player.openContainer instanceof GridContainer) {
                    IGrid grid = ((GridContainer) player.openContainer).getGrid();

                    if (grid.getGridType() == GridType.PATTERN) {
                        BaseItemHandler handler = ((GridNetworkNode) grid).getProcessingMatrix();
                        FluidInventory handlerFluid = ((GridNetworkNode) grid).getProcessingMatrixFluids();

                        clearInputsAndOutputs(handler);
                        clearInputsAndOutputs(handlerFluid);

                        setInputs(handler, message.inputs);
                        setOutputs(handler, message.outputs);

                        setFluidInputs(handlerFluid, message.fluidInputs);
                        setFluidOutputs(handlerFluid, message.fluidOutputs);

                        ((GridNetworkNode) grid).setProcessingPattern(true);
                        ((GridNetworkNode) grid).markDirty();
                    }
                }
            });
        }

        ctx.get().setPacketHandled(true);
    }

    private static void clearInputsAndOutputs(BaseItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); ++i) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private static void clearInputsAndOutputs(FluidInventory handler) {
        for (int i = 0; i < handler.getSlots(); ++i) {
            handler.setFluid(i, FluidStack.EMPTY);
        }
    }

    private static void setInputs(BaseItemHandler handler, Collection<ItemStack> stacks) {
        setSlots(handler, stacks, 0, handler.getSlots()/2);
    }

    private static void setOutputs(BaseItemHandler handler, Collection<ItemStack> stacks) {
        setSlots(handler, stacks, handler.getSlots()/2, handler.getSlots());
    }

    private static void setSlots(BaseItemHandler handler, Collection<ItemStack> stacks, int begin, int end) {
        for (ItemStack stack : stacks) {
            handler.setStackInSlot(begin, stack);

            begin++;

            if (begin >= end) {
                break;
            }
        }
    }

    private static void setFluidInputs(FluidInventory inventory, Collection<FluidStack> stacks) {
        setFluidSlots(inventory, stacks, 0, inventory.getSlots()/2);
    }

    private static void setFluidOutputs(FluidInventory inventory, Collection<FluidStack> stacks) {
        setFluidSlots(inventory, stacks, inventory.getSlots()/2, inventory.getSlots());
    }

    private static void setFluidSlots(FluidInventory inventory, Collection<FluidStack> stacks, int begin, int end) {
        for (FluidStack stack : stacks) {

            inventory.setFluid(begin, stack.copy());

            begin++;

            if (begin >= end) {
                break;
            }
        }
    }
}
