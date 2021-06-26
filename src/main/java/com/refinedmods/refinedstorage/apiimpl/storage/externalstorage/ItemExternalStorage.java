package com.refinedmods.refinedstorage.apiimpl.storage.externalstorage;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.AccessType;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorage;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorageContext;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ItemExternalStorage implements IExternalStorage<ItemStack> {
    private final IExternalStorageContext context;
    private final Supplier<IItemHandler> handlerSupplier;
    private final boolean connectedToInterface;
    private final ItemExternalStorageCache cache = new ItemExternalStorageCache();

    public ItemExternalStorage(IExternalStorageContext context, Supplier<IItemHandler> handlerSupplier, boolean connectedToInterface) {
        this.context = context;
        this.handlerSupplier = handlerSupplier;
        this.connectedToInterface = connectedToInterface;
    }

    public boolean isConnectedToInterface() {
        return connectedToInterface;
    }

    @Override
    public void update(INetwork network) {
        if (getAccessType() == AccessType.INSERT) {
            return;
        }

        cache.update(network, handlerSupplier.get());
    }

    @Override
    public long getCapacity() {
        IItemHandler handler = handlerSupplier.get();

        if (handler == null) {
            return 0;
        }

        long capacity = 0;

        for (int i = 0; i < handler.getSlots(); ++i) {
            capacity += handler.getSlotLimit(i);
        }

        return capacity;
    }

    @Override
    public Collection<ItemStack> getStacks() {
        IItemHandler handler = handlerSupplier.get();

        if (handler == null) {
            return Collections.emptyList();
        }

        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < handler.getSlots(); ++i) {
            stacks.add(handler.getStackInSlot(i));
        }

        return stacks;
    }

    @Override
    @Nonnull
    public ItemStack insert(@Nonnull ItemStack stack, int size, Action action) {
        if (stack.isEmpty()) {
            return stack;
        }

        IItemHandler handler = handlerSupplier.get();

        if (handler != null && context.acceptsItem(stack)) {
            return ItemHandlerHelper.insertItem(handler, ItemHandlerHelper.copyStackWithSize(stack, size), action == Action.SIMULATE);
        }

        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    @Override
    @Nonnull
    public ItemStack extract(@Nonnull ItemStack stack, int size, int flags, Action action) {
        if (stack.isEmpty()) {
            return stack;
        }

        IItemHandler handler = handlerSupplier.get();

        if (handler == null) {
            return ItemStack.EMPTY;
        }

        int remaining = size;

        ItemStack received = ItemStack.EMPTY;

        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack slot = handler.getStackInSlot(i);

            if (!slot.isEmpty() && API.instance().getComparer().isEqual(slot, stack, flags)) {
                ItemStack got = handler.extractItem(i, remaining, action == Action.SIMULATE);

                if (!got.isEmpty()) {
                    if (received.isEmpty()) {
                        received = got.copy();
                    } else {
                        received.grow(got.getCount());
                    }

                    remaining -= got.getCount();

                    if (remaining == 0) {
                        break;
                    }
                }
            }
        }

        return received;
    }

    @Override
    public int getStored() {
        return cache.getStored();
    }

    @Override
    public int getPriority() {
        return context.getPriority();
    }

    @Override
    public AccessType getAccessType() {
        return context.getAccessType();
    }

    @Override
    public int getCacheDelta(int storedPreInsertion, int size, @Nullable ItemStack remainder) {
        if (getAccessType() == AccessType.INSERT) {
            return 0;
        }

        return remainder == null ? size : (size - remainder.getCount());
    }
}
