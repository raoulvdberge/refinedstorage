package com.raoulvdberge.refinedstorage.integration.forgeenergy;

import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReader;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandler;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IWriter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ReaderWriterHandlerForgeEnergy implements IReaderWriterHandler {
    public static final String ID = "forgeenergy";

    private static final String NBT_ENERGY_STORED = "EnergyStored";

    private EnergyStorage storage;
    private EnergyStorageReaderWriter storageReader, storageWriter;

    public ReaderWriterHandlerForgeEnergy(@Nullable NBTTagCompound tag) {
        this.storage = new EnergyStorage(4000);
        this.storageReader = new EnergyStorageReaderWriter(storage, false, true);
        this.storageWriter = new EnergyStorageReaderWriter(storage, true, false);

        if (tag != null && tag.hasKey(NBT_ENERGY_STORED)) {
            storage.receiveEnergy(tag.getInteger(NBT_ENERGY_STORED), false);
        }
    }

    @Override
    public void update(IReaderWriterChannel channel) {
    }

    @Override
    public void onWriterDisabled(IWriter writer) {
    }

    @Override
    public boolean hasCapabilityReader(IReader reader, Capability<?> capability) {
        return capability == CapabilityEnergy.ENERGY;
    }

    @Override
    public <T> T getCapabilityReader(IReader reader, Capability<T> capability) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(storageReader);
        }

        return null;
    }

    @Override
    public boolean hasCapabilityWriter(IWriter writer, Capability<?> capability) {
        return capability == CapabilityEnergy.ENERGY;
    }

    @Override
    public <T> T getCapabilityWriter(IWriter writer, Capability<T> capability) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(storageWriter);
        }

        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger(NBT_ENERGY_STORED, storage.getEnergyStored());

        return tag;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<ITextComponent> getStatusReader(IReader reader, IReaderWriterChannel channel) {
        return getStatus(storageReader);
    }

    @Override
    public List<ITextComponent> getStatusWriter(IWriter writer, IReaderWriterChannel channel) {
        return getStatus(storageWriter);
    }

    private List<ITextComponent> getStatus(IEnergyStorage storage) {
        if (storage.getEnergyStored() == 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new TextComponentString(storage.getEnergyStored() + " FE / " + storage.getMaxEnergyStored() + " FE"));
    }

    private class EnergyStorageReaderWriter implements IEnergyStorage {
        private IEnergyStorage parent;
        private boolean canExtract, canReceive;

        EnergyStorageReaderWriter(IEnergyStorage parent, boolean canExtract, boolean canReceive) {
            this.canExtract = canExtract;
            this.canReceive = canReceive;
            this.parent = parent;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return canReceive ? parent.receiveEnergy(maxReceive, simulate) : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return canExtract ? parent.extractEnergy(maxExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return parent.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return parent.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return canExtract;
        }

        @Override
        public boolean canReceive() {
            return canReceive;
        }
    }
}
