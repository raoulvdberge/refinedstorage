package com.raoulvdberge.refinedstorage.gui.grid.stack;

import com.raoulvdberge.refinedstorage.api.storage.IStorageTracker;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class GridStackFluid implements IGridStack {
    private int hash;
    private FluidStack stack;
    @Nullable
    private IStorageTracker.IStorageTrackerEntry entry;

    public GridStackFluid(Pair<Integer, FluidStack> data, @Nullable IStorageTracker.IStorageTrackerEntry entry) {
        this.hash = data.getLeft();
        this.stack = data.getRight();
        this.entry = entry;
    }

    public FluidStack getStack() {
        return stack;
    }

    @Override
    public int getHash() {
        return hash;
    }

    @Override
    public String getName() {
        return stack.getFluid().getLocalizedName(stack);
    }

    @Override
    public String getModId() {
        return stack.getFluid().getStill(stack).getResourceDomain();
    }

    @Override
    public String[] getOreIds() {
        return new String[]{stack.getFluid().getName()};
    }

    @Override
    public String getTooltip() {
        return stack.getFluid().getLocalizedName(stack);
    }

    @Override
    public int getQuantity() {
        return stack.amount;
    }

    @Override
    public String getFormattedFullQuantity() {
        return API.instance().getQuantityFormatter().format(getQuantity()) + " mB";
    }

    @Override
    public void draw(GuiBase gui, int x, int y) {
        GuiBase.FLUID_RENDERER.draw(gui.mc, x, y, stack);

        float amountRaw = ((float) stack.amount / 1000F);
        int amount = (int) amountRaw;
        String formattedAmount = amount >= 1 ? API.instance().getQuantityFormatter().formatWithUnits(amount) : String.format("%.1f", amountRaw);
        
        gui.drawQuantity(x, y, formattedAmount);
    }

    @Override
    public Object getIngredient() {
        return stack;
    }

    @Nullable
    @Override
    public IStorageTracker.IStorageTrackerEntry getTrackerEntry() {
        return entry;
    }

    @Override
    public void setTrackerEntry(@Nullable IStorageTracker.IStorageTrackerEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GridStackFluid && ((GridStackFluid) obj).getStack().isFluidEqual(stack);
    }
}
