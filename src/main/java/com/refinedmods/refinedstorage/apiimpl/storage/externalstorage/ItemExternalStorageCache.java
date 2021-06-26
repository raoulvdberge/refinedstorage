package com.refinedmods.refinedstorage.apiimpl.storage.externalstorage;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemExternalStorageCache {
    private List<ItemStack> cache;
    private int stored = 0;

    public int getStored() {
        return stored;
    }

    public void update(INetwork network, @Nullable IItemHandler handler) {
        if (handler == null) {
            stored = 0;
            return;
        }

        if (cache == null) {
            cache = new ArrayList<>();

            int stored = 0;
            for (int i = 0; i < handler.getSlots(); ++i) {
                ItemStack stack = handler.getStackInSlot(i).copy();
                cache.add(stack);
                stored += stack.getCount();
            }
            this.stored = stored;

            return;
        }

        int stored = 0;
        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack actual = handler.getStackInSlot(i);
            stored += actual.getCount();

            if (i >= cache.size()) { // ENLARGED
                if (!actual.isEmpty()) {
                    network.getItemStorageCache().add(actual, actual.getCount(), false, true);

                    cache.add(actual.copy());
                }

                continue;
            }

            ItemStack cached = cache.get(i);

            if (!cached.isEmpty() && actual.isEmpty()) { // REMOVED
                network.getItemStorageCache().remove(cached, cached.getCount(), true);

                cache.set(i, ItemStack.EMPTY);
            } else if (cached.isEmpty() && !actual.isEmpty()) { // ADDED
                network.getItemStorageCache().add(actual, actual.getCount(), false, true);

                cache.set(i, actual.copy());
            } else if (!API.instance().getComparer().isEqualNoQuantity(cached, actual)) { // CHANGED
                network.getItemStorageCache().remove(cached, cached.getCount(), true);
                network.getItemStorageCache().add(actual, actual.getCount(), false, true);

                cache.set(i, actual.copy());
            } else if (cached.getCount() != actual.getCount()) { // COUNT_CHANGED
                int delta = actual.getCount() - cached.getCount();

                if (delta > 0) {
                    network.getItemStorageCache().add(actual, delta, false, true);

                    cached.grow(delta);
                } else {
                    network.getItemStorageCache().remove(actual, Math.abs(delta), true);

                    cached.shrink(Math.abs(delta));
                }
            }
        }
        this.stored = stored;

        if (cache.size() > handler.getSlots()) { // SHRUNK
            for (int i = cache.size() - 1; i >= handler.getSlots(); --i) { // Reverse order for the remove call.
                ItemStack cached = cache.get(i);

                if (!cached.isEmpty()) {
                    network.getItemStorageCache().remove(cached, cached.getCount(), true);
                }

                cache.remove(i);
            }
        }

        network.getItemStorageCache().flush();
    }
}
