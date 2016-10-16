package refinedstorage.tile.externalstorage;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import mcmultipart.microblock.IMicroblock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import refinedstorage.RS;
import refinedstorage.RSUtils;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.storage.AccessType;
import refinedstorage.api.storage.fluid.IFluidStorage;
import refinedstorage.api.storage.fluid.IFluidStorageProvider;
import refinedstorage.api.storage.item.IItemStorage;
import refinedstorage.api.storage.item.IItemStorageProvider;
import refinedstorage.api.util.IComparer;
import refinedstorage.inventory.ItemHandlerBasic;
import refinedstorage.inventory.ItemHandlerFluid;
import refinedstorage.tile.IStorageGui;
import refinedstorage.tile.TileMultipartNode;
import refinedstorage.tile.config.*;
import refinedstorage.tile.data.ITileDataProducer;
import refinedstorage.tile.data.TileDataParameter;

import java.util.ArrayList;
import java.util.List;

public class TileExternalStorage extends TileMultipartNode implements IItemStorageProvider, IFluidStorageProvider, IStorageGui, IComparable, IFilterable, IPrioritizable, IType, IAccessType {
    public static final TileDataParameter<Integer> PRIORITY = IPrioritizable.createParameter();
    public static final TileDataParameter<Integer> COMPARE = IComparable.createParameter();
    public static final TileDataParameter<Integer> MODE = IFilterable.createParameter();
    public static final TileDataParameter<Integer> TYPE = IType.createParameter();
    public static final TileDataParameter<AccessType> ACCESS_TYPE = IAccessType.createParameter();

    public static final TileDataParameter<Integer> STORED = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileExternalStorage>() {
        @Override
        public Integer getValue(TileExternalStorage tile) {
            int stored = 0;

            for (ItemStorageExternal storage : tile.itemStorages) {
                stored += storage.getStored();
            }

            for (FluidStorageExternal storage : tile.fluidStorages) {
                stored += storage.getStored();
            }

            return stored;
        }
    });

    public static final TileDataParameter<Integer> CAPACITY = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileExternalStorage>() {
        @Override
        public Integer getValue(TileExternalStorage tile) {
            int capacity = 0;

            for (ItemStorageExternal storage : tile.itemStorages) {
                capacity += storage.getCapacity();
            }

            for (FluidStorageExternal storage : tile.fluidStorages) {
                capacity += storage.getCapacity();
            }

            return capacity;
        }
    });

    private static final String NBT_PRIORITY = "Priority";
    private static final String NBT_COMPARE = "Compare";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_TYPE = "Type";

    private ItemHandlerBasic itemFilters = new ItemHandlerBasic(9, this);
    private ItemHandlerFluid fluidFilters = new ItemHandlerFluid(9, this);

    private int priority = 0;
    private int compare = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;
    private int mode = IFilterable.WHITELIST;
    private int type = IType.ITEMS;
    private AccessType accessType = AccessType.READ_WRITE;

    private List<ItemStorageExternal> itemStorages = new ArrayList<>();
    private List<FluidStorageExternal> fluidStorages = new ArrayList<>();

    private int lastDrawerCount;

    public TileExternalStorage() {
        dataManager.addWatchedParameter(PRIORITY);
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addWatchedParameter(MODE);
        dataManager.addWatchedParameter(STORED);
        dataManager.addWatchedParameter(CAPACITY);
        dataManager.addWatchedParameter(TYPE);
        dataManager.addWatchedParameter(ACCESS_TYPE);
    }

    @Override
    public boolean canAddMicroblock(IMicroblock microblock) {
        return !isBlockingMicroblock(microblock, getDirection());
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.externalStorageUsage + ((itemStorages.size() + fluidStorages.size()) * RS.INSTANCE.config.externalStoragePerStorageUsage);
    }

    @Override
    public void updateNode() {
    }

    @Override
    public void onConnectionChange(INetworkMaster network, boolean state) {
        super.onConnectionChange(network, state);

        updateStorage(network);

        network.getItemStorageCache().invalidate();
        network.getFluidStorageCache().invalidate();
    }

    @Override
    public void update() {
        if (!worldObj.isRemote && network != null) {
            boolean itemChangeDetected = false, fluidChangeDetected = false;

            for (ItemStorageExternal storage : itemStorages) {
                if (storage.updateCache()) {
                    itemChangeDetected = true;
                }
            }

            for (FluidStorageExternal storage : fluidStorages) {
                if (storage.updateCache()) {
                    fluidChangeDetected = true;
                }
            }

            if (itemChangeDetected) {
                network.getItemStorageCache().invalidate();
            }

            if (fluidChangeDetected) {
                network.getFluidStorageCache().invalidate();
            }

            if (getFacingTile() instanceof IDrawerGroup && lastDrawerCount != ((IDrawerGroup) getFacingTile()).getDrawerCount()) {
                lastDrawerCount = ((IDrawerGroup) getFacingTile()).getDrawerCount();

                updateStorage(network);
            }
        }

        super.update();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(itemFilters, 0, tag);
        RSUtils.readItems(fluidFilters, 1, tag);

        if (tag.hasKey(NBT_PRIORITY)) {
            priority = tag.getInteger(NBT_PRIORITY);
        }

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }

        if (tag.hasKey(NBT_MODE)) {
            mode = tag.getInteger(NBT_MODE);
        }

        if (tag.hasKey(NBT_TYPE)) {
            type = tag.getInteger(NBT_TYPE);
        }

        accessType = RSUtils.readAccessType(tag);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(itemFilters, 0, tag);
        RSUtils.writeItems(fluidFilters, 1, tag);

        tag.setInteger(NBT_PRIORITY, priority);
        tag.setInteger(NBT_COMPARE, compare);
        tag.setInteger(NBT_MODE, mode);
        tag.setInteger(NBT_TYPE, type);

        RSUtils.writeAccessType(tag, accessType);

        return tag;
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
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;

        markDirty();
    }

    public void updateStorage(INetworkMaster network) {
        itemStorages.clear();
        fluidStorages.clear();

        TileEntity facing = getFacingTile();

        if (facing instanceof IDrawerGroup) {
            IDrawerGroup group = (IDrawerGroup) facing;

            for (int i = 0; i < group.getDrawerCount(); ++i) {
                if (group.isDrawerEnabled(i)) {
                    itemStorages.add(new ItemStorageDrawer(this, group.getDrawer(i)));
                }
            }
        } else if (facing instanceof IDrawer) {
            itemStorages.add(new ItemStorageDrawer(this, (IDrawer) facing));
        } else if (facing instanceof IDeepStorageUnit) {
            itemStorages.add(new ItemStorageDSU(this, (IDeepStorageUnit) facing));
        } else {
            IItemHandler itemHandler = RSUtils.getItemHandler(facing, getDirection().getOpposite());

            if (itemHandler != null) {
                itemStorages.add(new ItemStorageItemHandler(this, itemHandler));
            }

            IFluidHandler fluidHandler = RSUtils.getFluidHandler(facing, getDirection().getOpposite());

            if (fluidHandler != null) {
                for (IFluidTankProperties property : fluidHandler.getTankProperties()) {
                    fluidStorages.add(new FluidStorageExternal(this, fluidHandler, property));
                }
            }
        }

        network.getItemStorageCache().invalidate();
        network.getFluidStorageCache().invalidate();
    }

    @Override
    public void addItemStorages(List<IItemStorage> storages) {
        storages.addAll(this.itemStorages);
    }

    @Override
    public void addFluidStorages(List<IFluidStorage> storages) {
        storages.addAll(this.fluidStorages);
    }

    @Override
    public String getGuiTitle() {
        return "gui.refinedstorage:external_storage";
    }

    @Override
    public TileDataParameter<Integer> getRedstoneModeParameter() {
        return REDSTONE_MODE;
    }

    @Override
    public TileDataParameter<Integer> getCompareParameter() {
        return COMPARE;
    }

    @Override
    public TileDataParameter<Integer> getFilterParameter() {
        return MODE;
    }

    @Override
    public TileDataParameter<Integer> getPriorityParameter() {
        return PRIORITY;
    }

    @Override
    public TileDataParameter<Boolean> getVoidExcessParameter() {
        return null;
    }

    @Override
    public TileDataParameter<AccessType> getAccessTypeParameter() {
        return ACCESS_TYPE;
    }

    @Override
    public String getVoidExcessType() {
        return null;
    }

    @Override
    public int getStored() {
        return STORED.getValue();
    }

    @Override
    public int getCapacity() {
        return CAPACITY.getValue();
    }

    @Override
    public AccessType getAccessType() {
        return accessType;
    }

    @Override
    public void setAccessType(AccessType type) {
        this.accessType = type;

        if (network != null) {
            network.getItemStorageCache().invalidate();
            network.getFluidStorageCache().invalidate();
        }

        markDirty();
    }

    @Override
    public TileDataParameter<Integer> getTypeParameter() {
        return TYPE;
    }

    @Override
    public int getType() {
        return worldObj.isRemote ? TYPE.getValue() : type;
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

    public ItemHandlerBasic getItemFilters() {
        return itemFilters;
    }

    public ItemHandlerFluid getFluidFilters() {
        return fluidFilters;
    }
}
