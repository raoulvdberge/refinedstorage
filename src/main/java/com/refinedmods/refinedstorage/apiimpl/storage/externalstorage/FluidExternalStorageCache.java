package com.refinedmods.refinedstorage.apiimpl.storage.externalstorage;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidExternalStorageCache {
    private List<FluidStack> cache;
    private int stored = 0;

    public int getStored() {
        return stored;
    }

    public void update(INetwork network, @Nullable IFluidHandler handler) {
        if (handler == null) {
            stored = 0;
            return;
        }

        if (cache == null) {
            cache = new ArrayList<>();

            int stored = 0;
            for (int i = 0; i < handler.getTanks(); ++i) {
                FluidStack stack = handler.getFluidInTank(i).copy();
                cache.add(stack);
                stored += stack.getAmount();
            }
            this.stored = stored;

            return;
        }

        int stored = 0;
        for (int i = 0; i < handler.getTanks(); ++i) {
            FluidStack actual = handler.getFluidInTank(i);
            stored += actual.getAmount();

            if (i >= cache.size()) { // ENLARGED
                if (!actual.isEmpty()) {
                    network.getFluidStorageCache().add(actual, actual.getAmount(), false, true);

                    cache.add(actual.copy());
                }

                continue;
            }

            FluidStack cached = cache.get(i);

            if (actual.isEmpty() && cached.isEmpty()) { // NONE
                continue;
            }

            if (actual.isEmpty() && !cached.isEmpty()) { // REMOVED
                network.getFluidStorageCache().remove(cached, cached.getAmount(), true);

                cache.set(i, FluidStack.EMPTY);
            } else if (!actual.isEmpty() && cached.isEmpty()) { // ADDED
                network.getFluidStorageCache().add(actual, actual.getAmount(), false, true);

                cache.set(i, actual.copy());
            } else if (!API.instance().getComparer().isEqual(actual, cached, IComparer.COMPARE_NBT)) { // CHANGED
                network.getFluidStorageCache().remove(cached, cached.getAmount(), true);
                network.getFluidStorageCache().add(actual, actual.getAmount(), false, true);

                cache.set(i, actual.copy());
            } else if (actual.getAmount() > cached.getAmount()) { // COUNT_CHANGED
                network.getFluidStorageCache().add(actual, actual.getAmount() - cached.getAmount(), false, true);

                cached.setAmount(actual.getAmount());
            } else if (actual.getAmount() < cached.getAmount()) { // COUNT_CHANGED
                network.getFluidStorageCache().remove(actual, cached.getAmount() - actual.getAmount(), true);

                cached.setAmount(actual.getAmount());
            }
        }
        this.stored = stored;

        if (cache.size() > handler.getTanks()) { // SHRUNK
            for (int i = cache.size() - 1; i >= handler.getTanks(); --i) { // Reverse order for the remove call.
                FluidStack cached = cache.get(i);

                if (!cached.isEmpty()) {
                    network.getFluidStorageCache().remove(cached, cached.getAmount(), true);
                }

                cache.remove(i);
            }
        }

        network.getFluidStorageCache().flush();
    }
}
