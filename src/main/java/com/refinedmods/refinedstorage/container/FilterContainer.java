package com.refinedmods.refinedstorage.container;

import com.refinedmods.refinedstorage.RSContainers;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.inventory.fluid.FilterFluidInventory;
import com.refinedmods.refinedstorage.inventory.fluid.FilterIconFluidInventory;
import com.refinedmods.refinedstorage.inventory.fluid.FluidInventory;
import com.refinedmods.refinedstorage.inventory.item.FilterIconItemHandler;
import com.refinedmods.refinedstorage.inventory.item.FilterItemsItemHandler;
import com.refinedmods.refinedstorage.item.FilterItem;
import com.refinedmods.refinedstorage.tile.config.IType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class FilterContainer extends BaseContainer {
    private ItemStack stack;

    public FilterContainer(PlayerEntity player, ItemStack stack, int windowId) {
        super(RSContainers.FILTER, null, player, windowId);

        this.stack = stack;

        int y = 20;
        int x = 8;

        FilterItemsItemHandler filter = new FilterItemsItemHandler(stack);
        FluidInventory fluidFilter = new FilterFluidInventory(stack);

        for (int i = 0; i < 27; ++i) {
            addSlot(new FilterSlot(filter, i, x, y).setEnableHandler(() -> FilterItem.getType(stack) == IType.ITEMS));
            addSlot(new FluidFilterSlot(fluidFilter, i, x, y).setEnableHandler(() -> FilterItem.getType(stack) == IType.FLUIDS));

            if ((i + 1) % 9 == 0) {
                x = 8;
                y += 18;
            } else {
                x += 18;
            }
        }

        addSlot(new FilterSlot(new FilterIconItemHandler(stack), 0, 8, 117).setEnableHandler(() -> FilterItem.getType(stack) == IType.ITEMS));
        addSlot(new FluidFilterSlot(new FilterIconFluidInventory(stack), 0, 8, 117).setEnableHandler(() -> FilterItem.getType(stack) == IType.FLUIDS));

        addPlayerInventory(8, 149);

        transferManager.addFilterTransfer(player.inventory, filter, fluidFilter, () -> FilterItem.getType(stack));
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected int getDisabledSlotNumber() {
        return getPlayer().inventory.currentItem;
    }
}
