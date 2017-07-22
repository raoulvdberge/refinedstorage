package com.raoulvdberge.refinedstorage.network;

import com.raoulvdberge.refinedstorage.api.network.grid.GridType;
import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.api.network.security.Permission;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeGrid;
import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageGridClear extends MessageHandlerPlayerToServer<MessageGridClear> implements IMessage {
    public MessageGridClear() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // NO OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // NO OP
    }

    @Override
    public void handle(MessageGridClear message, EntityPlayerMP player) {
        Container container = player.openContainer;

        if (container instanceof ContainerGrid) {
            IGrid grid = ((ContainerGrid) container).getGrid();

            InventoryCrafting matrix = grid.getCraftingMatrix();

            if (grid.getType() == GridType.CRAFTING && grid.getNetwork() != null && grid.getNetwork().getSecurityManager().hasPermission(Permission.INSERT, player)) {
                for (int i = 0; i < matrix.getSizeInventory(); ++i) {
                    ItemStack slot = matrix.getStackInSlot(i);

                    if (!slot.isEmpty()) {
                        matrix.setInventorySlotContents(i, StackUtils.nullToEmpty(grid.getNetwork().insertItem(slot, slot.getCount(), false)));
                    }
                }
            } else if (grid.getType() == GridType.PATTERN) {
                ((NetworkNodeGrid) grid).onPatternMatrixClear();
            }
        }
    }
}
