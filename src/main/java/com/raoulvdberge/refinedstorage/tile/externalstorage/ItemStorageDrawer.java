package com.raoulvdberge.refinedstorage.tile.externalstorage;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.IVoidable;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.tile.config.IFilterable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collections;
import java.util.List;

public class ItemStorageDrawer extends ItemStorageExternal {
    private TileExternalStorage externalStorage;
    private IDrawer drawer;

    public ItemStorageDrawer(TileExternalStorage externalStorage, IDrawer drawer) {
        this.externalStorage = externalStorage;
        this.drawer = drawer;
    }

    @Override
    public int getCapacity() {
        return drawer.getMaxCapacity();
    }

    @Override
    public List<ItemStack> getItems() {
        if (!drawer.isEmpty() && drawer.getStoredItemCount() > 0) {
            return Collections.singletonList(drawer.getStoredItemCopy());
        }

        return Collections.emptyList();
    }

    private boolean isVoidable() {
        return drawer instanceof IVoidable && ((IVoidable) drawer).isVoid();
    }

    @Override
    public ItemStack insertItem(ItemStack stack, int size, boolean simulate) {
        if (IFilterable.canTake(externalStorage.getItemFilters(), externalStorage.getMode(), externalStorage.getCompare(), stack) && drawer.canItemBeStored(stack)) {
            int stored = drawer.getStoredItemCount();
            int remainingSpace = drawer.getMaxCapacity(stack) - stored;

            int inserted = remainingSpace > size ? size : (remainingSpace <= 0) ? 0 : remainingSpace;

            if (!simulate && remainingSpace > 0) {
                if (drawer.isEmpty()) {
                    drawer.setStoredItemRedir(stack, inserted);
                } else {
                    drawer.setStoredItemCount(stored + inserted);
                }
            }

            if (inserted == size) {
                return null;
            }

            int returnSize = size - inserted;

            if (isVoidable()) {
                returnSize = -returnSize;
            }

            return ItemHandlerHelper.copyStackWithSize(stack, returnSize);
        }

        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    @Override
    public ItemStack extractItem(ItemStack stack, int size, int flags) {
        if (API.instance().getComparer().isEqual(stack, drawer.getStoredItemPrototype(), flags) && drawer.canItemBeExtracted(stack)) {
            if (size > drawer.getStoredItemCount()) {
                size = drawer.getStoredItemCount();
            }

            ItemStack stored = drawer.getStoredItemPrototype();

            drawer.setStoredItemCount(drawer.getStoredItemCount() - size);

            return ItemHandlerHelper.copyStackWithSize(stored, size);
        }

        return null;
    }

    @Override
    public int getStored() {
        return drawer.getStoredItemCount();
    }

    @Override
    public int getPriority() {
        return externalStorage.getPriority();
    }

    @Override
    public AccessType getAccessType() {
        return externalStorage.getAccessType();
    }
}
