package com.refinedmods.refinedstorage.apiimpl.storage.tracker;

import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTracker;
import com.refinedmods.refinedstorage.api.storage.tracker.StorageTrackerEntry;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class FluidStorageTracker implements IStorageTracker<FluidStack> {
    private static final String NBT_STACK = "Stack";
    private static final String NBT_NAME = "Name";
    private static final String NBT_TIME = "Time";

    private final Map<Key, StorageTrackerEntry> changes = new HashMap<>();
    private final Runnable listener;

    public FluidStorageTracker(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void changed(PlayerEntity player, FluidStack stack) {
        changes.put(new Key(stack), new StorageTrackerEntry(System.currentTimeMillis(), player.getName().getString()));

        listener.run();
    }

    @Override
    public StorageTrackerEntry get(FluidStack stack) {
        return changes.get(new Key(stack));
    }

    @Override
    public void readFromNbt(ListNBT list) {
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT tag = list.getCompound(i);

            FluidStack stack = FluidStack.loadFluidStackFromNBT(tag.getCompound(NBT_STACK));

            if (!stack.isEmpty()) {
                changes.put(new Key(stack), new StorageTrackerEntry(tag.getLong(NBT_TIME), tag.getString(NBT_NAME)));
            }
        }
    }

    @Override
    public ListNBT serializeNbt() {
        ListNBT list = new ListNBT();

        for (Map.Entry<Key, StorageTrackerEntry> entry : changes.entrySet()) {
            CompoundNBT tag = new CompoundNBT();

            tag.putLong(NBT_TIME, entry.getValue().getTime());
            tag.putString(NBT_NAME, entry.getValue().getName());
            tag.put(NBT_STACK, entry.getKey().stack.writeToNBT(new CompoundNBT()));

            list.add(tag);
        }

        return list;
    }

    private static class Key {
        private final FluidStack stack;

        public Key(FluidStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key && API.instance().getComparer().isEqual(stack, ((Key) other).stack, IComparer.COMPARE_NBT);
        }

        @Override
        public int hashCode() {
            return API.instance().getFluidStackHashCode(stack);
        }
    }
}
