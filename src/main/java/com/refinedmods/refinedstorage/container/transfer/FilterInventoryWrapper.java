package com.refinedmods.refinedstorage.container.transfer;

import com.refinedmods.refinedstorage.inventory.fluid.FluidInventory;
import com.refinedmods.refinedstorage.tile.config.IType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Supplier;

class FilterInventoryWrapper implements IInventoryWrapper {
    private ItemFilterInventoryWrapper item;
    private FluidFilterInventoryWrapper fluid;
    private Supplier<Integer> typeGetter;

    FilterInventoryWrapper(IItemHandlerModifiable itemTo, FluidInventory fluidTo, Supplier<Integer> typeGetter) {
        this.item = new ItemFilterInventoryWrapper(itemTo);
        this.fluid = new FluidFilterInventoryWrapper(fluidTo);
        this.typeGetter = typeGetter;
    }

    @Override
    public InsertionResult insert(ItemStack stack) {
        return typeGetter.get() == IType.ITEMS ? item.insert(stack) : fluid.insert(stack);
    }
}
