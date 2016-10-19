package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBasic;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerFluid;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileFluidInterface extends TileNode implements IComparable {
    public static final int TANK_CAPACITY = 16000;

    public static final TileDataParameter<Integer> COMPARE = IComparable.createParameter();

    public static final TileDataParameter<FluidStack> TANK_IN = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return tile.tankIn.getFluid();
        }
    });

    public static final TileDataParameter<FluidStack> TANK_OUT = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return tile.tankOut.getFluid();
        }
    });

    private static final String NBT_COMPARE = "Compare";
    private static final String NBT_TANK_IN = "TankIn";
    private static final String NBT_TANK_OUT = "TankOut";

    private int compare = IComparer.COMPARE_NBT;

    private FluidTank tankIn = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();

            if (worldObj != null && !worldObj.isRemote) {
                dataManager.sendParameterToWatchers(TANK_IN);
            }

            markDirty();
        }
    };

    private FluidTank tankOut = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();

            if (worldObj != null && !worldObj.isRemote) {
                dataManager.sendParameterToWatchers(TANK_OUT);
            }

            markDirty();
        }
    };

    private ItemHandlerBasic in = new ItemHandlerBasic(1, this);
    private ItemHandlerFluid out = new ItemHandlerFluid(1, this);

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, this, ItemUpgrade.TYPE_SPEED, ItemUpgrade.TYPE_STACK);

    public TileFluidInterface() {
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addParameter(TANK_IN);
        dataManager.addParameter(TANK_OUT);

        tankIn.setCanDrain(false);
        tankIn.setCanFill(true);

        tankOut.setCanDrain(true);
        tankOut.setCanFill(false);
    }

    @Override
    public void updateNode() {
        ItemStack container = in.getStackInSlot(0);

        if (container != null) {
            FluidStack fluid = RSUtils.getFluidFromStack(container, true);

            if (fluid != null && tankIn.fillInternal(fluid, false) == fluid.amount) {
                tankIn.fillInternal(RSUtils.getFluidFromStack(container, false), true);
            }
        }

        if (ticks % upgrades.getSpeed() == 0) {
            FluidStack drained = tankIn.drainInternal(Fluid.BUCKET_VOLUME * upgrades.getInteractStackSize(), true);

            // Drain in tank
            if (drained != null) {
                FluidStack remainder = network.insertFluid(drained, drained.amount, false);

                if (remainder != null) {
                    tankIn.fillInternal(remainder, true);
                }
            }

            FluidStack stack = out.getFluidStackInSlot(0);

            // Fill out tank

            // If our out fluid doesn't match the new fluid, empty it first
            if (tankOut.getFluid() != null && (stack == null || (tankOut.getFluid().getFluid() != stack.getFluid()))) {
                FluidStack remainder = tankOut.drainInternal(Fluid.BUCKET_VOLUME * upgrades.getInteractStackSize(), true);

                if (remainder != null) {
                    network.insertFluid(remainder, remainder.amount, false);
                }
            } else if (stack != null) {
                // Fill the out fluid
                FluidStack stackInStorage = network.getFluidStorageCache().getList().get(stack, compare);

                if (stackInStorage != null) {
                    int toExtract = Math.min(Fluid.BUCKET_VOLUME * upgrades.getInteractStackSize(), stackInStorage.amount);

                    FluidStack took = network.extractFluid(stack, toExtract, compare);

                    if (took != null) {
                        int remainder = toExtract - tankOut.fillInternal(took, true);

                        if (remainder > 0) {
                            network.insertFluid(took, remainder, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.fluidInterfaceUsage;
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(upgrades, 0, tag);
        RSUtils.writeItems(in, 1, tag);
        RSUtils.writeItems(out, 2, tag);

        tag.setTag(NBT_TANK_IN, tankIn.writeToNBT(new NBTTagCompound()));
        tag.setTag(NBT_TANK_OUT, tankOut.writeToNBT(new NBTTagCompound()));

        tag.setInteger(NBT_COMPARE, compare);

        return tag;
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(upgrades, 0, tag);
        RSUtils.readItems(in, 1, tag);
        RSUtils.readItems(out, 2, tag);

        if (tag.hasKey(NBT_TANK_IN)) {
            tankIn.readFromNBT(tag.getCompoundTag(NBT_TANK_IN));
        }

        if (tag.hasKey(NBT_TANK_OUT)) {
            tankOut.readFromNBT(tag.getCompoundTag(NBT_TANK_OUT));
        }

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }
    }

    public ItemHandlerUpgrade getUpgrades() {
        return upgrades;
    }

    public ItemHandlerBasic getIn() {
        return in;
    }

    public ItemHandlerFluid getOut() {
        return out;
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return facing == EnumFacing.DOWN ? (T) tankOut : (T) tankIn;
        }

        return super.getCapability(capability, facing);
    }
}
