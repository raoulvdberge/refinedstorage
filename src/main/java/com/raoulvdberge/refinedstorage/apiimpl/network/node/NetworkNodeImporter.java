package com.raoulvdberge.refinedstorage.apiimpl.network.node;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.INetworkNodeHolder;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBasic;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerFluid;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerListenerNetworkNode;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.TileDiskDrive;
import com.raoulvdberge.refinedstorage.tile.TileImporter;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.tile.config.IFilterable;
import com.raoulvdberge.refinedstorage.tile.config.IType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class NetworkNodeImporter extends NetworkNode implements IComparable, IFilterable, IType {
    public static final String ID = "importer";

    private static final String NBT_COMPARE = "Compare";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_TYPE = "Type";

    private ItemHandlerBasic itemFilters = new ItemHandlerBasic(9, new ItemHandlerListenerNetworkNode(this));
    private ItemHandlerFluid fluidFilters = new ItemHandlerFluid(9, new ItemHandlerListenerNetworkNode(this));

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, new ItemHandlerListenerNetworkNode(this), ItemUpgrade.TYPE_SPEED, ItemUpgrade.TYPE_STACK);

    private int compare = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;
    private int mode = IFilterable.WHITELIST;
    private int type = IType.ITEMS;

    private int currentSlot;

    public NetworkNodeImporter(INetworkNodeHolder holder) {
        super(holder);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.importerUsage + upgrades.getEnergyUsage();
    }

    @Override
    public void update() {
        if (network == null) {
            return;
        }
        
        if (type == IType.ITEMS) {
            TileEntity tile = holder.world().getTileEntity(holder.pos().offset(holder.getDirection()));
            IItemHandler handler = RSUtils.getItemHandler(tile, holder.getDirection().getOpposite());

            if (handler == null || tile instanceof TileDiskDrive) {
                return;
            }

            if (currentSlot >= handler.getSlots()) {
                currentSlot = 0;
            }

            if (handler.getSlots() > 0) {
                ItemStack stack = handler.getStackInSlot(currentSlot);

                if (stack.isEmpty() || !IFilterable.canTake(itemFilters, mode, compare, stack)) {
                    currentSlot++;
                } else if (ticks % upgrades.getSpeed() == 0) {
                    ItemStack result = handler.extractItem(currentSlot, upgrades.getItemInteractCount(), true);

                    if (!result.isEmpty() && network.insertItem(result, result.getCount(), true) == null) {
                        network.insertItemTracked(result, result.getCount());

                        handler.extractItem(currentSlot, upgrades.getItemInteractCount(), false);
                    } else {
                        currentSlot++;
                    }
                }
            }
        } else if (type == IType.FLUIDS && ticks % upgrades.getSpeed() == 0) {
            IFluidHandler handler = RSUtils.getFluidHandler(holder.world().getTileEntity(holder.pos().offset(holder.getDirection())), holder.getDirection().getOpposite());

            if (handler != null) {
                FluidStack stack = handler.drain(Fluid.BUCKET_VOLUME, false);

                if (stack != null && IFilterable.canTakeFluids(fluidFilters, mode, compare, stack) && network.insertFluid(stack, stack.amount, true) == null) {
                    FluidStack toDrain = handler.drain(Fluid.BUCKET_VOLUME * upgrades.getItemInteractCount(), false);

                    if (toDrain != null) {
                        FluidStack remainder = network.insertFluid(toDrain, toDrain.amount, false);
                        if (remainder != null) {
                            toDrain.amount -= remainder.amount;
                        }
                        handler.drain(toDrain, true);
                    }
                }
            }
        }
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;

        markDirty();
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;

        markDirty();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(upgrades, 1, tag);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(upgrades, 1, tag);

        return tag;
    }


    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        tag.setInteger(NBT_COMPARE, compare);
        tag.setInteger(NBT_MODE, mode);
        tag.setInteger(NBT_TYPE, type);

        RSUtils.writeItems(itemFilters, 0, tag);
        RSUtils.writeItems(fluidFilters, 2, tag);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }

        if (tag.hasKey(NBT_MODE)) {
            mode = tag.getInteger(NBT_MODE);
        }

        if (tag.hasKey(NBT_TYPE)) {
            type = tag.getInteger(NBT_TYPE);
        }

        RSUtils.readItems(itemFilters, 0, tag);
        RSUtils.readItems(fluidFilters, 2, tag);
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public IItemHandler getDrops() {
        return upgrades;
    }

    @Override
    public int getType() {
        return holder.world().isRemote ? TileImporter.TYPE.getValue() : type;
    }

    @Override
    public void setType(int type) {
        this.type = type;

        markDirty();
    }

    @Override
    public IItemHandler getFilterInventory() {
        return getType() == IType.ITEMS ? itemFilters : fluidFilters;
    }
}
