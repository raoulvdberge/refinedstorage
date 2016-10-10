package refinedstorage.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.fluids.FluidStack;
import refinedstorage.RS;
import refinedstorage.RSBlocks;
import refinedstorage.RSUtils;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.storage.fluid.IFluidStorage;
import refinedstorage.api.storage.fluid.IFluidStorageProvider;
import refinedstorage.api.util.IComparer;
import refinedstorage.apiimpl.storage.fluid.FluidStorageNBT;
import refinedstorage.block.BlockFluidStorage;
import refinedstorage.block.EnumFluidStorageType;
import refinedstorage.inventory.ItemHandlerFluid;
import refinedstorage.tile.config.*;
import refinedstorage.tile.data.ITileDataProducer;
import refinedstorage.tile.data.TileDataParameter;

import java.util.List;

public class TileFluidStorage extends TileNode implements IFluidStorageProvider, IStorageGui, IComparable, IFilterable, IPrioritizable, IExcessVoidable, IAccessType{
    public static final TileDataParameter<Integer> PRIORITY = IPrioritizable.createParameter();
    public static final TileDataParameter<Integer> COMPARE = IComparable.createParameter();
    public static final TileDataParameter<Boolean> VOID_EXCESS = IExcessVoidable.createParameter();
    public static final TileDataParameter<Integer> MODE = IFilterable.createParameter();
    public static final TileDataParameter<Integer> ACCESS_TYPE = IAccessType.createParameter();
    public static final TileDataParameter<Integer> STORED = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileFluidStorage>() {
        @Override
        public Integer getValue(TileFluidStorage tile) {
            return FluidStorageNBT.getStoredFromNBT(tile.storageTag);
        }
    });

    class FluidStorage extends FluidStorageNBT {
        public FluidStorage() {
            super(TileFluidStorage.this.getStorageTag(), TileFluidStorage.this.getCapacity(), TileFluidStorage.this);
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public FluidStack insertFluid(FluidStack stack, int size, boolean simulate) {
            if (!IFilterable.canTakeFluids(filters, mode, compare, stack)) {
                return RSUtils.copyStackWithSize(stack, size);
            }
			
            FluidStack result = super.insertFluid(stack, size, simulate);
            if (voidExcess && result != null) {
                // Simulate should not matter as the fluids are voided anyway
                result.amount = -result.amount;
            }

            return result;
        }

        @Override
        public int getAccessType() {
            return accessType;
        }
    }

    public static final String NBT_STORAGE = "Storage";

    private static final String NBT_PRIORITY = "Priority";
    private static final String NBT_COMPARE = "Compare";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_VOID_EXCESS = "VoidExcess";
    private static final String NBT_ACCESS_TYPE = "AccessType";

    private ItemHandlerFluid filters = new ItemHandlerFluid(9, this);

    private NBTTagCompound storageTag = FluidStorageNBT.createNBT();

    private FluidStorage storage;

    private EnumFluidStorageType type;

    private int accessType = IAccessType.READ_WRITE;
    private int priority = 0;
    private int compare = IComparer.COMPARE_NBT;
    private int mode = IFilterable.WHITELIST;
    private boolean voidExcess = false;

    public TileFluidStorage() {
        dataManager.addWatchedParameter(PRIORITY);
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addWatchedParameter(MODE);
        dataManager.addWatchedParameter(STORED);
        dataManager.addWatchedParameter(VOID_EXCESS);
        dataManager.addWatchedParameter(ACCESS_TYPE);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.fluidStorageUsage;
    }

    @Override
    public void updateNode() {
    }

    @Override
    public void update() {
        super.update();

        if (storage == null && storageTag != null) {
            storage = new FluidStorage();

            if (network != null) {
                network.getFluidStorage().rebuild();
            }
        }
    }

    public void onBreak() {
        if (storage != null) {
            storage.writeToNBT();
        }
    }

    @Override
    public void onConnectionChange(INetworkMaster network, boolean state) {
        super.onConnectionChange(network, state);

        network.getFluidStorage().rebuild();
    }

    @Override
    public void addFluidStorages(List<IFluidStorage> storages) {
        if (storage != null) {
            storages.add(storage);
        }
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(filters, 0, tag);

        if (tag.hasKey(NBT_PRIORITY)) {
            priority = tag.getInteger(NBT_PRIORITY);
        }

        if (tag.hasKey(NBT_STORAGE)) {
            storageTag = tag.getCompoundTag(NBT_STORAGE);
        }

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }

        if (tag.hasKey(NBT_MODE)) {
            mode = tag.getInteger(NBT_MODE);
        }

        if (tag.hasKey(NBT_VOID_EXCESS)) {
            voidExcess = tag.getBoolean(NBT_VOID_EXCESS);
        }

        if (tag.hasKey(NBT_ACCESS_TYPE)) {
            accessType = tag.getInteger(NBT_ACCESS_TYPE);
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(filters, 0, tag);

        tag.setInteger(NBT_PRIORITY, priority);

        if (storage != null) {
            storage.writeToNBT();
        }

        tag.setTag(NBT_STORAGE, storageTag);
        tag.setInteger(NBT_COMPARE, compare);
        tag.setInteger(NBT_MODE, mode);
        tag.setBoolean(NBT_VOID_EXCESS, voidExcess);
        tag.setInteger(NBT_ACCESS_TYPE, accessType);

        return tag;
    }

    public EnumFluidStorageType getType() {
        if (type == null && worldObj.getBlockState(pos).getBlock() == RSBlocks.FLUID_STORAGE) {
            this.type = ((EnumFluidStorageType) worldObj.getBlockState(pos).getValue(BlockFluidStorage.TYPE));
        }

        return type == null ? EnumFluidStorageType.TYPE_64K : type;
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
    public String getGuiTitle() {
        return "block.refinedstorage:fluid_storage." + getType().getId() + ".name";
    }

    @Override
    public TileDataParameter<Integer> getTypeParameter() {
        return null;
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
        return VOID_EXCESS;
    }

    @Override
    public  TileDataParameter<Integer> getAccessTypeParameter() {
        return ACCESS_TYPE;
    }

    @Override
    public String getVoidExcessType() {
        return "fluids";
    }

    public NBTTagCompound getStorageTag() {
        return storageTag;
    }

    public void setStorageTag(NBTTagCompound storageTag) {
        this.storageTag = storageTag;
    }

    public FluidStorageNBT getStorage() {
        return storage;
    }

    public ItemHandlerFluid getFilters() {
        return filters;
    }

    @Override
    public int getAccessType() {
        return accessType;
    }

    @Override
    public void setAccessType(int value) {
        accessType = value;

        network.getFluidStorage().rebuild();

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

    @Override
    public int getStored() {
        return STORED.getValue();
    }

    @Override
    public int getCapacity() {
        return getType().getCapacity();
    }

    @Override
    public boolean getVoidExcess() {
        return voidExcess;
    }

    @Override
    public void setVoidExcess(boolean value) {
        this.voidExcess = value;

        markDirty();
    }
}

