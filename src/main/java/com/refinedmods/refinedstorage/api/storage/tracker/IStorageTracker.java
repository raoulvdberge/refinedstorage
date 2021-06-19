package com.refinedmods.refinedstorage.api.storage.tracker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;

/**
 * Keeps track of when a stack is changed in the system.
 */
public interface IStorageTracker<T> {
    /**
     * Updates the storage tracker entry for a stack, changing it's player name and modification time.
     *
     * @param player player
     * @param stack  the stack
     */
    void changed(PlayerEntity player, T stack);

    /**
     * @param stack the stack
     * @return the entry, or null if the stack hasn't been modified yet
     */
    @Nullable
    StorageTrackerEntry get(T stack);

    /**
     * initialize tracker from nbt
     *
     * @param nbt to read from
     */
    void readFromNbt(ListNBT nbt);

    /**
     * write data to nbt
     *
     * @return data as nbt
     */
    ListNBT serializeNbt();
}
