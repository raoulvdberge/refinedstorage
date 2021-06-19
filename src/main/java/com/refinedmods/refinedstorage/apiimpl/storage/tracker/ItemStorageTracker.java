package com.refinedmods.refinedstorage.apiimpl.storage.tracker;

import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTracker;
import com.refinedmods.refinedstorage.api.storage.tracker.StorageTrackerEntry;
import com.refinedmods.refinedstorage.util.ItemStackKey;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.HashMap;
import java.util.Map;

public class ItemStorageTracker implements IStorageTracker<ItemStack> {
    private static final String NBT_STACK = "Stack";
    private static final String NBT_NAME = "Name";
    private static final String NBT_TIME = "Time";

    private final Map<ItemStackKey, StorageTrackerEntry> changes = new HashMap<>();
    private final Runnable listener;

    public ItemStorageTracker(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void changed(PlayerEntity player, ItemStack stack) {
        changes.put(new ItemStackKey(stack), new StorageTrackerEntry(System.currentTimeMillis(), player.getName().getString()));

        listener.run();
    }

    @Override
    public StorageTrackerEntry get(ItemStack stack) {
        return changes.get(new ItemStackKey(stack));
    }

    @Override
    public void readFromNbt(ListNBT list) {
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT tag = list.getCompound(i);

            ItemStack stack = StackUtils.deserializeStackFromNbt(tag.getCompound(NBT_STACK));

            if (!stack.isEmpty()) {
                changes.put(new ItemStackKey(stack), new StorageTrackerEntry(tag.getLong(NBT_TIME), tag.getString(NBT_NAME)));
            }
        }
    }

    @Override
    public ListNBT serializeNbt() {
        ListNBT list = new ListNBT();

        for (Map.Entry<ItemStackKey, StorageTrackerEntry> entry : changes.entrySet()) {
            CompoundNBT tag = new CompoundNBT();

            tag.putLong(NBT_TIME, entry.getValue().getTime());
            tag.putString(NBT_NAME, entry.getValue().getName());
            tag.put(NBT_STACK, StackUtils.serializeStackToNbt(entry.getKey().getStack()));

            list.add(tag);
        }

        return list;
    }
}
