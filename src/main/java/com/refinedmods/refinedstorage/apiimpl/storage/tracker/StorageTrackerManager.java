package com.refinedmods.refinedstorage.apiimpl.storage.tracker;

import com.refinedmods.refinedstorage.api.storage.StorageType;
import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTracker;
import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTrackerManager;
import com.refinedmods.refinedstorage.apiimpl.util.RSWorldSavedData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageTrackerManager extends RSWorldSavedData implements IStorageTrackerManager {
    public static final String NAME = "refinedstorage_tracker";

    private static final String NBT_TRACKERS = "Tracker";
    private static final String NBT_TRACKER_ID = "Id";
    private static final String NBT_TRACKER_DATA = "Data";
    private static final String NBT_TRACKER_TYPE = "Type";

    private final Map<UUID, IStorageTracker<?>> trackers = new HashMap<>();

    public StorageTrackerManager(String name) {
        super(name);
    }

    @Override
    public void markForSaving() {
        this.markDirty();
    }


    @Override
    public IStorageTracker<?> getOrCreate(UUID uuid, StorageType type) {
        IStorageTracker<?> tracker = trackers.get(uuid);

        if (tracker == null) {
            if (type == StorageType.ITEM) {
                tracker = new ItemStorageTracker(this::markForSaving);
            } else if (type == StorageType.FLUID) {
                tracker = new FluidStorageTracker(this::markForSaving);
            }

            trackers.put(uuid, tracker);
        }

        return tracker;
    }

    @Override
    public void remove(UUID id) {
        trackers.remove(id);
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (nbt.contains(NBT_TRACKERS)) {
            ListNBT trackerTags = nbt.getList(NBT_TRACKERS, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < trackerTags.size(); ++i) {
                CompoundNBT trackerTag = trackerTags.getCompound(i);

                UUID id = trackerTag.getUniqueId(NBT_TRACKER_ID);
                ListNBT data = trackerTag.getList(NBT_TRACKER_DATA, Constants.NBT.TAG_COMPOUND);
                StorageType type = StorageType.values()[trackerTag.getInt(NBT_TRACKER_TYPE)];

                IStorageTracker<?> tracker = getOrCreate(id, type);
                tracker.readFromNbt(data);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT trackerListTag = new ListNBT();

        for (Map.Entry<UUID, IStorageTracker<?>> entry : trackers.entrySet()) {
            CompoundNBT trackerTag = new CompoundNBT();

            trackerTag.putUniqueId(NBT_TRACKER_ID, entry.getKey());
            trackerTag.put(NBT_TRACKER_DATA, entry.getValue().serializeNbt());
            trackerTag.putInt(NBT_TRACKER_TYPE, entry.getValue() instanceof ItemStorageTracker ? StorageType.ITEM.ordinal() : StorageType.FLUID.ordinal());

            trackerListTag.add(trackerTag);
        }

        compound.put(NBT_TRACKERS, trackerListTag);

        return compound;
    }
}
