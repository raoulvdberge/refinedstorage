package com.raoulvdberge.refinedstorage.apiimpl.network.node.diskdrive;

import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDiskContainerContext;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDiskListener;
import com.raoulvdberge.refinedstorage.tile.TileDiskDrive;
import com.raoulvdberge.refinedstorage.tile.config.IFilterable;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import com.raoulvdberge.refinedstorage.util.WorldUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class StorageDiskFluidDriveWrapper implements IStorageDisk<FluidStack> {
    private NetworkNodeDiskDrive diskDrive;
    private IStorageDisk<FluidStack> parent;
    private int lastState;

    public StorageDiskFluidDriveWrapper(NetworkNodeDiskDrive diskDrive, IStorageDisk<FluidStack> parent) {
        this.diskDrive = diskDrive;
        this.parent = parent;
        this.setSettings(
            () -> {
                int currentState = TileDiskDrive.getDiskState(getStored(), getCapacity());

                if (lastState != currentState) {
                    lastState = currentState;

                    WorldUtils.updateBlock(diskDrive.getWorld(), diskDrive.getPos());
                }
            },
            diskDrive
        );
        this.lastState = TileDiskDrive.getDiskState(getStored(), getCapacity());
    }

    @Override
    public int getPriority() {
        return diskDrive.getPriority();
    }

    @Override
    public AccessType getAccessType() {
        return parent.getAccessType();
    }

    @Override
    public Collection<FluidStack> getStacks() {
        return parent.getStacks();
    }

    @Override
    @Nullable
    public FluidStack insert(@Nonnull FluidStack stack, int size, boolean simulate) {
        if (!IFilterable.canTakeFluids(diskDrive.getFluidFilters(), diskDrive.getMode(), diskDrive.getCompare(), stack)) {
            return StackUtils.copy(stack, size);
        }

        return parent.insert(stack, size, simulate);
    }

    @Nullable
    @Override
    public FluidStack extract(@Nonnull FluidStack stack, int size, int flags, boolean simulate) {
        return parent.extract(stack, size, flags, simulate);
    }

    @Override
    public int getStored() {
        return parent.getStored();
    }

    @Override
    public int getCacheDelta(int storedPreInsertion, int size, @Nullable FluidStack remainder) {
        return parent.getCacheDelta(storedPreInsertion, size, remainder);
    }

    @Override
    public int getCapacity() {
        return parent.getCapacity();
    }

    @Override
    public void setSettings(@Nullable IStorageDiskListener listener, IStorageDiskContainerContext context) {
        parent.setSettings(listener, context);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        return parent.writeToNbt();
    }

    @Override
    public String getId() {
        return parent.getId();
    }
}