package com.refinedmods.refinedstorage.apiimpl.util;

import com.google.common.collect.ArrayListMultimap;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.api.util.StackListResult;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemStackList implements IStackList<ItemStack> {
    private final ArrayListMultimap<Item, StackListEntry<ItemStack>> stacks = ArrayListMultimap.create();
    private final Map<UUID, ItemStack> index = new HashMap<>();

    public ItemStackList() {
    }

    public ItemStackList(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            add(stack);
        }
    }

    @Override
    public StackListResult<ItemStack> add(@Nonnull ItemStack stack, int size) {
        if (stack.isEmpty() || size <= 0) {
            throw new IllegalArgumentException("Cannot accept empty stack");
        }

        for (StackListEntry<ItemStack> entry : stacks.get(stack.getItem())) {
            ItemStack otherStack = entry.getStack();

            if (API.instance().getComparer().isEqualNoQuantity(otherStack, stack)) {
                if ((long) otherStack.getCount() + (long) size > Integer.MAX_VALUE) {
                    otherStack.setCount(Integer.MAX_VALUE);
                } else {
                    otherStack.grow(size);
                }

                return new StackListResult<>(otherStack, entry.getId(), size);
            }
        }

        StackListEntry<ItemStack> newEntry = new StackListEntry<>(ItemHandlerHelper.copyStackWithSize(stack, size));

        stacks.put(stack.getItem(), newEntry);
        index.put(newEntry.getId(), newEntry.getStack());

        return new StackListResult<>(newEntry.getStack(), newEntry.getId(), size);
    }

    @Override
    public StackListResult<ItemStack> add(@Nonnull ItemStack stack) {
        return add(stack, stack.getCount());
    }

    @Override
    public StackListResult<ItemStack> remove(@Nonnull ItemStack stack, int size) {
        for (StackListEntry<ItemStack> entry : stacks.get(stack.getItem())) {
            ItemStack otherStack = entry.getStack();

            if (API.instance().getComparer().isEqualNoQuantity(otherStack, stack)) {
                if (otherStack.getCount() - size <= 0) {
                    stacks.remove(otherStack.getItem(), entry);
                    index.remove(entry.getId());

                    return new StackListResult<>(otherStack, entry.getId(), -otherStack.getCount());
                } else {
                    otherStack.shrink(size);

                    return new StackListResult<>(otherStack, entry.getId(), -size);
                }
            }
        }

        return null;
    }

    @Override
    public StackListResult<ItemStack> remove(@Nonnull ItemStack stack) {
        return remove(stack, stack.getCount());
    }

    @Override
    public int getCount(@Nonnull ItemStack stack, int flags) {
        ItemStack found = get(stack, flags);
        if (found == null) {
            return 0;
        }

        return found.getCount();
    }

    @Override
    @Nullable
    public ItemStack get(@Nonnull ItemStack stack, int flags) {
        for (StackListEntry<ItemStack> entry : stacks.get(stack.getItem())) {
            ItemStack otherStack = entry.getStack();

            if (API.instance().getComparer().isEqual(otherStack, stack, flags)) {
                return otherStack;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public StackListEntry<ItemStack> getEntry(@Nonnull ItemStack stack, int flags) {
        for (StackListEntry<ItemStack> entry : stacks.get(stack.getItem())) {
            ItemStack otherStack = entry.getStack();

            if (API.instance().getComparer().isEqual(otherStack, stack, flags)) {
                return entry;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public ItemStack get(UUID id) {
        return index.get(id);
    }

    @Override
    public void clear() {
        stacks.clear();
        index.clear();
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<StackListEntry<ItemStack>> getStacks() {
        return stacks.values();
    }

    @Override
    @Nonnull
    public Collection<StackListEntry<ItemStack>> getStacks(@Nonnull ItemStack stack) {
        return stacks.get(stack.getItem());
    }

    @Override
    @Nonnull
    public IStackList<ItemStack> copy() {
        ItemStackList list = new ItemStackList();

        for (StackListEntry<ItemStack> entry : stacks.values()) {
            ItemStack newStack = entry.getStack().copy();

            list.stacks.put(entry.getStack().getItem(), new StackListEntry<>(entry.getId(), newStack));
            list.index.put(entry.getId(), newStack);
        }

        return list;
    }

    @Override
    public int size() {
        return stacks.size();
    }
}
