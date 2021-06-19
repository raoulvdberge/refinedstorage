package com.refinedmods.refinedstorage.api.storage.tracker;

import com.refinedmods.refinedstorage.api.storage.StorageType;

import java.util.UUID;

public interface IStorageTrackerManager {

    /**
     * Marks this manager for saving.
     */
    void markForSaving();

    /**
     * get a storage tracker or create a new Storage Tracker if it doesn't exist
     *
     * @param itemStorageTrackerId UUID for that tracker
     * @param type                 Storage type of that tracker
     * @return StorageTracker
     */
    IStorageTracker<?> getOrCreate(UUID itemStorageTrackerId, StorageType type);

    /**
     * @param id of the Storage tracker that will be removed
     */
    void remove(UUID id);
}
