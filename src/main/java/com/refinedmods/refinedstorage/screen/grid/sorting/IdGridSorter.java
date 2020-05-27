package com.refinedmods.refinedstorage.screen.grid.sorting;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.screen.grid.stack.IGridStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;

public class IdGridSorter implements IGridSorter {
    @Override
    public boolean isApplicable(IGrid grid) {
        return grid.getSortingType() == IGrid.SORTING_TYPE_ID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int compare(IGridStack left, IGridStack right, SortingDirection sortingDirection) {
        int leftId = 0;
        int rightId = 0;

        if (left.getIngredient() instanceof ItemStack && right.getIngredient() instanceof ItemStack) {
            leftId = Item.getIdFromItem(((ItemStack) left.getIngredient()).getItem());
            rightId = Item.getIdFromItem(((ItemStack) right.getIngredient()).getItem());
        } else if (left.getIngredient() instanceof FluidStack && right.getIngredient() instanceof FluidStack) {
            leftId = Registry.FLUID.getId(((FluidStack) left.getIngredient()).getFluid());
            rightId = Registry.FLUID.getId(((FluidStack) right.getIngredient()).getFluid());
        }

        if (leftId != rightId) {
            if (sortingDirection == SortingDirection.DESCENDING) {
                return Integer.compare(leftId, rightId);
            } else if (sortingDirection == SortingDirection.ASCENDING) {
                return Integer.compare(rightId, leftId);
            }
        }

        return 0;
    }
}