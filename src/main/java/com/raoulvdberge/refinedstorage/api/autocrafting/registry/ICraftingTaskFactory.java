package com.raoulvdberge.refinedstorage.api.autocrafting.registry;

import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A factory that creates a crafting task.
 * Register this factory in the {@link ICraftingTaskRegistry}.
 */
public interface ICraftingTaskFactory {
    /**
     * Returns a crafting task for a given NBT tag and pattern.
     *
     * @param network  the network
     * @param stack    the stack to create a task for
     * @param pattern  the pattern
     * @param quantity the quantity
     * @param tag      the NBT tag, if this is null it isn't reading from disk but is used for making a task on demand
     * @return the crafting task
     */
    @Nonnull
    ICraftingTask create(INetwork network, ItemStack stack, int quantity, ICraftingPattern pattern, @Nullable NBTTagCompound tag);
}
