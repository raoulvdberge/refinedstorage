package com.refinedmods.refinedstorage.inventory.item;

import com.refinedmods.refinedstorage.RSItems;
import com.refinedmods.refinedstorage.api.network.grid.IGridTab;
import com.refinedmods.refinedstorage.api.util.IFilter;
import com.refinedmods.refinedstorage.apiimpl.network.grid.GridTab;
import com.refinedmods.refinedstorage.apiimpl.util.FluidFilter;
import com.refinedmods.refinedstorage.apiimpl.util.ItemFilter;
import com.refinedmods.refinedstorage.inventory.fluid.FilterFluidInventory;
import com.refinedmods.refinedstorage.inventory.item.validator.ItemValidator;
import com.refinedmods.refinedstorage.item.FilterItem;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.ArrayList;
import java.util.List;

public class FilterItemHandler extends BaseItemHandler {
    private final List<IFilter> filters;
    private final List<IGridTab> tabs;

    public FilterItemHandler(List<IFilter> filters, List<IGridTab> tabs) {
        super(4);

        this.filters = filters;
        this.tabs = tabs;

        this.addValidator(new ItemValidator(RSItems.FILTER));
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        filters.clear();
        tabs.clear();

        for (int i = 0; i < getSlots(); ++i) {
            ItemStack filter = getStackInSlot(i);

            if (!filter.isEmpty()) {
                addFilter(filter);
            }
        }

        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort());
        }
    }

    private void addFilter(ItemStack filter) {
        int compare = FilterItem.getCompare(filter);
        int mode = FilterItem.getMode(filter);
        boolean modFilter = FilterItem.isModFilter(filter);

        List<IFilter> filters = new ArrayList<>();

        FilterItemsItemHandler items = new FilterItemsItemHandler(filter);

        for (ItemStack stack : items.getFilteredItems()) {
            if (stack.getItem() == RSItems.FILTER) {
                addFilter(stack);
            } else if (!stack.isEmpty()) {
                filters.add(new ItemFilter(stack, compare, mode, modFilter));
            }
        }

        FilterFluidInventory fluids = new FilterFluidInventory(filter);

        for (FluidStack stack : fluids.getFilteredFluids()) {
            filters.add(new FluidFilter(stack, compare, mode, modFilter));
        }

        ItemStack icon = FilterItem.getIcon(filter);
        FluidStack fluidIcon = FilterItem.getFluidIcon(filter);

        if (icon.isEmpty() && fluidIcon.isEmpty()) {
            this.filters.addAll(filters);
        } else {
            tabs.add(new GridTab(filters, FilterItem.getName(filter), icon, fluidIcon));
        }
    }
}
