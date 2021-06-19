package com.refinedmods.refinedstorage.tile.grid.portable;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.RSTiles;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.ICraftingGridListener;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.IGridTab;
import com.refinedmods.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.api.storage.AccessType;
import com.refinedmods.refinedstorage.api.storage.StorageType;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCache;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCacheListener;
import com.refinedmods.refinedstorage.api.storage.cache.InvalidateCause;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDiskContainerContext;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDiskProvider;
import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTracker;
import com.refinedmods.refinedstorage.api.util.IFilter;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.grid.handler.PortableFluidGridHandler;
import com.refinedmods.refinedstorage.apiimpl.network.grid.handler.PortableItemGridHandler;
import com.refinedmods.refinedstorage.apiimpl.network.node.DiskState;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.PortableFluidStorageCache;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.PortableItemStorageCache;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.PortableFluidGridStorageCacheListener;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.PortableItemGridStorageCacheListener;
import com.refinedmods.refinedstorage.apiimpl.storage.disk.PortableFluidStorageDisk;
import com.refinedmods.refinedstorage.apiimpl.storage.disk.PortableItemStorageDisk;
import com.refinedmods.refinedstorage.apiimpl.storage.tracker.FluidStorageTracker;
import com.refinedmods.refinedstorage.apiimpl.storage.tracker.ItemStorageTracker;
import com.refinedmods.refinedstorage.block.PortableGridBlock;
import com.refinedmods.refinedstorage.inventory.item.BaseItemHandler;
import com.refinedmods.refinedstorage.inventory.item.FilterItemHandler;
import com.refinedmods.refinedstorage.inventory.item.validator.StorageDiskItemValidator;
import com.refinedmods.refinedstorage.inventory.listener.TileInventoryListener;
import com.refinedmods.refinedstorage.item.WirelessGridItem;
import com.refinedmods.refinedstorage.item.blockitem.PortableGridBlockItem;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.tile.BaseTile;
import com.refinedmods.refinedstorage.tile.config.IRedstoneConfigurable;
import com.refinedmods.refinedstorage.tile.config.RedstoneMode;
import com.refinedmods.refinedstorage.tile.data.TileDataManager;
import com.refinedmods.refinedstorage.tile.data.TileDataParameter;
import com.refinedmods.refinedstorage.tile.grid.GridTile;
import com.refinedmods.refinedstorage.util.StackUtils;
import com.refinedmods.refinedstorage.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PortableGridTile extends BaseTile implements ITickableTileEntity, IGrid, IPortableGrid, IRedstoneConfigurable, IStorageDiskContainerContext {
    public static final TileDataParameter<Integer, PortableGridTile> REDSTONE_MODE = RedstoneMode.createParameter();
    private static final TileDataParameter<Integer, PortableGridTile> SORTING_DIRECTION = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getSortingDirection, (t, v) -> {
        if (IGrid.isValidSortingDirection(v)) {
            t.setSortingDirection(v);
            t.markDirty();
        }
    }, (initial, p) -> GridTile.trySortGrid(initial));
    private static final TileDataParameter<Integer, PortableGridTile> SORTING_TYPE = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getSortingType, (t, v) -> {
        if (IGrid.isValidSortingType(v)) {
            t.setSortingType(v);
            t.markDirty();
        }
    }, (initial, p) -> GridTile.trySortGrid(initial));
    private static final TileDataParameter<Integer, PortableGridTile> SEARCH_BOX_MODE = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getSearchBoxMode, (t, v) -> {
        if (IGrid.isValidSearchBoxMode(v)) {
            t.setSearchBoxMode(v);
            t.markDirty();
        }
    }, (initial, p) -> BaseScreen.executeLater(GridScreen.class, grid -> grid.getSearchField().setMode(p)));
    private static final TileDataParameter<Integer, PortableGridTile> SIZE = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getSize, (t, v) -> {
        if (IGrid.isValidSize(v)) {
            t.setSize(v);
            t.markDirty();
        }
    }, (initial, p) -> BaseScreen.executeLater(GridScreen.class, BaseScreen::init));
    private static final TileDataParameter<Integer, PortableGridTile> TAB_SELECTED = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getTabSelected, (t, v) -> {
        t.setTabSelected(v == t.getTabSelected() ? -1 : v);
        t.markDirty();
    }, (initial, p) -> BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort()));
    private static final TileDataParameter<Integer, PortableGridTile> TAB_PAGE = new TileDataParameter<>(DataSerializers.VARINT, 0, PortableGridTile::getTabPage, (t, v) -> {
        if (v >= 0 && v <= t.getTotalTabPages()) {
            t.setTabPage(v);
            t.markDirty();
        }
    });

    private static final String NBT_STORAGE_TRACKER = "StorageTracker"; //TODO: remove next version
    private static final String NBT_ITEM_STORAGE_TRACKER_ID = "ItemStorageTrackerId";
    private static final String NBT_FLUID_STORAGE_TRACKER = "FluidStorageTracker"; //TODO: remove next version
    private static final String NBT_FLUID_STORAGE_TRACKER_ID = "FluidStorageTrackerId";
    private static final String NBT_TYPE = "Type";
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_ENCHANTMENTS = "Enchantments"; // @Volatile: Minecraft specific nbt key, see EnchantmentHelper

    private EnergyStorage energyStorage = createEnergyStorage(0);
    private final LazyOptional<EnergyStorage> energyStorageCap = LazyOptional.of(() -> energyStorage);

    private final PortableGridBlockItem.Type type;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    private int sortingType;
    private int sortingDirection;
    private int searchBoxMode;
    private int tabSelected;
    private int tabPage;
    private int size;

    private GridType clientGridType;

    private final List<IFilter> filters = new ArrayList<>();
    private final List<IGridTab> tabs = new ArrayList<>();

    private final FilterItemHandler filter = (FilterItemHandler) new FilterItemHandler(filters, tabs).addListener(new TileInventoryListener(this));
    private final BaseItemHandler disk = new BaseItemHandler(1)
        .addValidator(new StorageDiskItemValidator())
        .addListener(new TileInventoryListener(this))
        .addListener((handler, slot, reading) -> {
            if (world != null && !world.isRemote) {
                loadStorage();

                if (!reading) {
                    updateState();

                    WorldUtils.updateBlock(world, pos); // Re-send grid type
                }
            }
        });

    @Nullable
    private IStorageDisk storage;
    @Nullable
    private IStorageCache cache;

    private final PortableItemGridHandler itemHandler = new PortableItemGridHandler(this, this);
    private final PortableFluidGridHandler fluidHandler = new PortableFluidGridHandler(this);

    private PortableGridDiskState diskState = PortableGridDiskState.NONE;
    private boolean active;

    private ItemStorageTracker itemStorageTracker;
    private UUID itemStorageTrackerId;
    private FluidStorageTracker fluidStorageTracker;
    private UUID fluidStorageTrackerId;

    private ListNBT enchants = null;

    private boolean loadNextTick;

    public PortableGridTile(PortableGridBlockItem.Type type) {
        super(type == PortableGridBlockItem.Type.CREATIVE ? RSTiles.CREATIVE_PORTABLE_GRID : RSTiles.PORTABLE_GRID);

        this.type = type;

        dataManager.addWatchedParameter(REDSTONE_MODE);
        dataManager.addWatchedParameter(SORTING_DIRECTION);
        dataManager.addWatchedParameter(SORTING_TYPE);
        dataManager.addWatchedParameter(SEARCH_BOX_MODE);
        dataManager.addWatchedParameter(SIZE);
        dataManager.addWatchedParameter(TAB_SELECTED);
        dataManager.addWatchedParameter(TAB_PAGE);
    }

    private void loadStorage() {
        ItemStack diskStack = getDiskInventory().getStackInSlot(0);

        if (diskStack.isEmpty()) {
            this.storage = null;
            this.cache = null;
        } else {
            IStorageDisk diskInSlot = API.instance().getStorageDiskManager((ServerWorld) world).getByStack(getDiskInventory().getStackInSlot(0));

            if (diskInSlot != null) {
                StorageType diskType = ((IStorageDiskProvider) getDiskInventory().getStackInSlot(0).getItem()).getType();

                if (diskType == StorageType.ITEM) {
                    this.storage = new PortableItemStorageDisk(diskInSlot, this);
                    this.cache = new PortableItemStorageCache(this);
                } else if (diskType == StorageType.FLUID) {
                    this.storage = new PortableFluidStorageDisk(diskInSlot, this);
                    this.cache = new PortableFluidStorageCache(this);
                }

                this.storage.setSettings(PortableGridTile.this::updateState, PortableGridTile.this);
            } else {
                this.storage = null;
                this.cache = null;
            }
        }

        if (cache != null) {
            cache.invalidate(InvalidateCause.DISK_INVENTORY_CHANGED);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.loadStorage();

        loadNextTick = true;
    }

    public void applyDataFromItemToTile(ItemStack stack) {
        this.sortingType = WirelessGridItem.getSortingType(stack);
        this.sortingDirection = WirelessGridItem.getSortingDirection(stack);
        this.searchBoxMode = WirelessGridItem.getSearchBoxMode(stack);
        this.tabSelected = WirelessGridItem.getTabSelected(stack);
        this.tabPage = WirelessGridItem.getTabPage(stack);
        this.size = WirelessGridItem.getSize(stack);

        this.energyStorage = createEnergyStorage(stack.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0));

        if (stack.hasTag()) {
            for (int i = 0; i < 4; ++i) {
                StackUtils.readItems(filter, i, stack.getTag());
            }

            StackUtils.readItems(disk, 4, stack.getTag());

            this.redstoneMode = RedstoneMode.read(stack.getTag());
            if (stack.getTag().contains(PortableGrid.NBT_ITEM_STORAGE_TRACKER_ID)) {
                itemStorageTrackerId = stack.getTag().getUniqueId(NBT_ITEM_STORAGE_TRACKER_ID);
            } else {
                if (stack.getTag().contains(PortableGrid.NBT_STORAGE_TRACKER)) { //TODO: remove next version
                    getItemStorageTracker().readFromNbt(stack.getTag().getList(PortableGrid.NBT_STORAGE_TRACKER, Constants.NBT.TAG_COMPOUND));
                }
            }

            if (stack.getTag().contains(PortableGrid.NBT_FLUID_STORAGE_TRACKER_ID)) {
                fluidStorageTrackerId = stack.getTag().getUniqueId(NBT_FLUID_STORAGE_TRACKER_ID);
            } else {
                if (stack.getTag().contains(PortableGrid.NBT_FLUID_STORAGE_TRACKER)) { //TODO: remove next version
                    getFluidStorageTracker().readFromNbt(stack.getTag().getList(PortableGrid.NBT_FLUID_STORAGE_TRACKER, Constants.NBT.TAG_COMPOUND));
                }
            }

            if (stack.getTag().contains(NBT_ENCHANTMENTS)) {
                enchants = stack.getTag().getList(NBT_ENCHANTMENTS, Constants.NBT.TAG_COMPOUND);
            }
        }

        markDirty();
    }

    public void applyDataFromTileToItem(ItemStack stack) {
        stack.setTag(new CompoundNBT());

        stack.getTag().putInt(GridNetworkNode.NBT_SORTING_DIRECTION, sortingDirection);
        stack.getTag().putInt(GridNetworkNode.NBT_SORTING_TYPE, sortingType);
        stack.getTag().putInt(GridNetworkNode.NBT_SEARCH_BOX_MODE, searchBoxMode);
        stack.getTag().putInt(GridNetworkNode.NBT_SIZE, size);
        stack.getTag().putInt(GridNetworkNode.NBT_TAB_SELECTED, tabSelected);
        stack.getTag().putInt(GridNetworkNode.NBT_TAB_PAGE, tabPage);

        if (itemStorageTrackerId != null) {
            stack.getTag().putUniqueId(PortableGrid.NBT_ITEM_STORAGE_TRACKER_ID, itemStorageTrackerId);
        }
        if (fluidStorageTrackerId != null) {
            stack.getTag().putUniqueId(PortableGrid.NBT_FLUID_STORAGE_TRACKER_ID, fluidStorageTrackerId);
        }

        if (enchants != null) {
            stack.getTag().put(NBT_ENCHANTMENTS, enchants);
        }

        stack.getCapability(CapabilityEnergy.ENERGY, null).ifPresent(itemEnergy -> itemEnergy.receiveEnergy(energyStorage.getEnergyStored(), false));

        for (int i = 0; i < 4; ++i) {
            StackUtils.writeItems(filter, i, stack.getTag());
        }

        StackUtils.writeItems(disk, 4, stack.getTag());

        redstoneMode.write(stack.getTag());
    }

    private EnergyStorage createEnergyStorage(int energyStored) {
        return new EnergyStorage(
            RS.SERVER_CONFIG.getPortableGrid().getCapacity(),
            RS.SERVER_CONFIG.getPortableGrid().getCapacity(),
            RS.SERVER_CONFIG.getPortableGrid().getCapacity(),
            energyStored
        );
    }

    @Override
    public GridType getGridType() {
        return clientGridType != null ? clientGridType : getServerGridType();
    }

    private GridType getServerGridType() {
        return (getDiskInventory().getStackInSlot(0).isEmpty() || ((IStorageDiskProvider) getDiskInventory().getStackInSlot(0).getItem()).getType() == StorageType.ITEM) ? GridType.NORMAL : GridType.FLUID;
    }

    @Nullable
    @Override
    public IStorageCache getStorageCache() {
        return storage != null ? cache : null;
    }

    @Override
    public IStorageCacheListener createListener(ServerPlayerEntity player) {
        return getServerGridType() == GridType.FLUID ? new PortableFluidGridStorageCacheListener(this, player) : new PortableItemGridStorageCacheListener(this, player);
    }

    @Nullable
    @Override
    public IItemGridHandler getItemHandler() {
        return itemHandler;
    }

    @Nullable
    @Override
    public IFluidGridHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public void addCraftingListener(ICraftingGridListener listener) {
        // NO OP
    }

    @Override
    public void removeCraftingListener(ICraftingGridListener listener) {
        // NO OP
    }

    @Override
    public ITextComponent getTitle() {
        return new TranslationTextComponent("gui.refinedstorage.portable_grid");
    }

    @Override
    public int getViewType() {
        return -1;
    }

    @Override
    public int getSortingType() {
        return world.isRemote ? SORTING_TYPE.getValue() : sortingType;
    }

    @Override
    public int getSortingDirection() {
        return world.isRemote ? SORTING_DIRECTION.getValue() : sortingDirection;
    }

    @Override
    public int getSearchBoxMode() {
        return world.isRemote ? SEARCH_BOX_MODE.getValue() : searchBoxMode;
    }

    @Override
    public int getTabSelected() {
        return world.isRemote ? TAB_SELECTED.getValue() : tabSelected;
    }

    @Override
    public int getTabPage() {
        return world.isRemote ? TAB_PAGE.getValue() : Math.min(tabPage, getTotalTabPages());
    }

    @Override
    public int getTotalTabPages() {
        return (int) Math.floor((float) Math.max(0, tabs.size() - 1) / (float) IGrid.TABS_PER_PAGE);
    }

    @Override
    public int getSize() {
        return world.isRemote ? SIZE.getValue() : size;
    }

    public void setSortingType(int sortingType) {
        this.sortingType = sortingType;
    }

    public void setSortingDirection(int sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void setSearchBoxMode(int searchBoxMode) {
        this.searchBoxMode = searchBoxMode;
    }

    public void setTabSelected(int tabSelected) {
        this.tabSelected = tabSelected;
    }

    public void setTabPage(int page) {
        this.tabPage = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void onViewTypeChanged(int type) {
        // NO OP
    }

    @Override
    public void onSortingTypeChanged(int type) {
        TileDataManager.setParameter(SORTING_TYPE, type);
    }

    @Override
    public void onSortingDirectionChanged(int direction) {
        TileDataManager.setParameter(SORTING_DIRECTION, direction);
    }

    @Override
    public void onSearchBoxModeChanged(int searchBoxMode) {
        TileDataManager.setParameter(SEARCH_BOX_MODE, searchBoxMode);
    }

    @Override
    public void onSizeChanged(int size) {
        TileDataManager.setParameter(SIZE, size);
    }

    @Override
    public void onTabSelectionChanged(int tab) {
        TileDataManager.setParameter(TAB_SELECTED, tab);
    }

    @Override
    public void onTabPageChanged(int page) {
        if (page >= 0 && page <= getTotalTabPages()) {
            TileDataManager.setParameter(PortableGridTile.TAB_PAGE, page);
        }
    }

    @Override
    public List<IFilter> getFilters() {
        return filters;
    }

    @Override
    public List<IGridTab> getTabs() {
        return tabs;
    }

    @Override
    public IItemHandlerModifiable getFilter() {
        return filter;
    }

    @Override
    public IStorageTracker<ItemStack> getItemStorageTracker() {
        if (itemStorageTracker == null) {
            if (itemStorageTrackerId == null) {
                this.itemStorageTrackerId = UUID.randomUUID();
            }

            this.itemStorageTracker = (ItemStorageTracker) API.instance().getStorageTrackerManager((ServerWorld) world).getOrCreate(itemStorageTrackerId, StorageType.ITEM);
        }

        return itemStorageTracker;
    }

    @Override
    public IStorageTracker<FluidStack> getFluidStorageTracker() {
        if (fluidStorageTracker == null) {
            if (fluidStorageTrackerId == null) {
                this.fluidStorageTrackerId = UUID.randomUUID();
            }

            this.fluidStorageTracker = (FluidStorageTracker) API.instance().getStorageTrackerManager((ServerWorld) world).getOrCreate(fluidStorageTrackerId, StorageType.FLUID);
        }

        return fluidStorageTracker;
    }

    @Override
    public CraftingInventory getCraftingMatrix() {
        return null;
    }

    @Override
    public CraftResultInventory getCraftingResult() {
        return null;
    }

    @Override
    public void onCraftingMatrixChanged() {
        // NO OP
    }

    @Override
    public void onCrafted(PlayerEntity player, @Nullable IStackList<ItemStack> availableItems, @Nullable IStackList<ItemStack> usedItems) {
        // NO OP
    }

    @Override
    public void onClear(PlayerEntity player) {
        // NO OP
    }

    @Override
    public void onCraftedShift(PlayerEntity player) {
        // NO OP
    }

    @Override
    public void onRecipeTransfer(PlayerEntity player, ItemStack[][] recipe) {
        // NO OP
    }

    @Override
    public void onClosed(PlayerEntity player) {
        // NO OP
    }

    private boolean hasDisk() {
        return !disk.getStackInSlot(0).isEmpty();
    }

    @Override
    public boolean isGridActive() {
        if (world.isRemote) {
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof PortableGridBlock) {
                return state.get(PortableGridBlock.ACTIVE);
            }

            return false;
        }

        if (RS.SERVER_CONFIG.getPortableGrid().getUseEnergy() &&
            type != PortableGridBlockItem.Type.CREATIVE &&
            energyStorage.getEnergyStored() <= RS.SERVER_CONFIG.getPortableGrid().getOpenUsage()) {
            return false;
        }

        if (!hasDisk()) {
            return false;
        }

        return redstoneMode.isEnabled(world.isBlockPowered(pos));
    }

    @Override
    public int getSlotId() {
        return -1;
    }

    @Override
    @Nullable
    public IStorageCache getCache() {
        return cache;
    }

    @Override
    @Nullable
    public IStorageDisk getStorage() {
        return storage;
    }

    @Override
    public void drainEnergy(int energy) {
        if (RS.SERVER_CONFIG.getPortableGrid().getUseEnergy() &&
            type != PortableGridBlockItem.Type.CREATIVE &&
            redstoneMode.isEnabled(world.isBlockPowered(pos))) {
            energyStorage.extractEnergy(energy, false);

            updateState();
        }
    }

    @Override
    public int getEnergy() {
        if (RS.SERVER_CONFIG.getPortableGrid().getUseEnergy() && type != PortableGridBlockItem.Type.CREATIVE) {
            return energyStorage.getEnergyStored();
        }

        return RS.SERVER_CONFIG.getPortableGrid().getCapacity();
    }

    @Override
    public PortableGridDiskState getDiskState() {
        if (!hasDisk()) {
            return PortableGridDiskState.NONE;
        }

        if (!isGridActive()) {
            return PortableGridDiskState.DISCONNECTED;
        }

        int stored = storage != null ? storage.getStored() : 0;
        int capacity = storage != null ? storage.getCapacity() : 0;

        if (stored == capacity) {
            return PortableGridDiskState.FULL;
        } else if ((int) ((float) stored / (float) capacity * 100F) >= DiskState.DISK_NEAR_CAPACITY_THRESHOLD) {
            return PortableGridDiskState.NEAR_CAPACITY;
        } else {
            return PortableGridDiskState.NORMAL;
        }
    }

    public void updateState() {
        PortableGridDiskState newDiskState = getDiskState();

        if (this.diskState != newDiskState) {
            this.diskState = newDiskState;

            world.setBlockState(pos, world.getBlockState(pos).with(PortableGridBlock.DISK_STATE, diskState));
        }

        boolean isActive = isGridActive();

        if (this.active != isActive) {
            this.active = isActive;

            world.setBlockState(pos, world.getBlockState(pos).with(PortableGridBlock.ACTIVE, active));
        }
    }

    @Override
    public BaseItemHandler getDiskInventory() {
        return disk;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putInt(GridNetworkNode.NBT_SORTING_DIRECTION, sortingDirection);
        tag.putInt(GridNetworkNode.NBT_SORTING_TYPE, sortingType);
        tag.putInt(GridNetworkNode.NBT_SEARCH_BOX_MODE, searchBoxMode);
        tag.putInt(GridNetworkNode.NBT_SIZE, size);
        tag.putInt(GridNetworkNode.NBT_TAB_SELECTED, tabSelected);
        tag.putInt(GridNetworkNode.NBT_TAB_PAGE, tabPage);

        StackUtils.writeItems(disk, 0, tag);
        StackUtils.writeItems(filter, 1, tag);

        tag.putInt(NBT_ENERGY, energyStorage.getEnergyStored());

        redstoneMode.write(tag);

        if (itemStorageTrackerId != null) {
            tag.putUniqueId(NBT_ITEM_STORAGE_TRACKER_ID, itemStorageTrackerId);
        }
        if (fluidStorageTrackerId != null) {
            tag.putUniqueId(NBT_FLUID_STORAGE_TRACKER_ID, fluidStorageTrackerId);
        }

        if (enchants != null) {
            tag.put(NBT_ENCHANTMENTS, enchants);
        }

        return tag;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT tag) {
        super.read(blockState, tag);

        if (tag.contains(GridNetworkNode.NBT_SORTING_DIRECTION)) {
            sortingDirection = tag.getInt(GridNetworkNode.NBT_SORTING_DIRECTION);
        }

        if (tag.contains(GridNetworkNode.NBT_SORTING_TYPE)) {
            sortingType = tag.getInt(GridNetworkNode.NBT_SORTING_TYPE);
        }

        if (tag.contains(GridNetworkNode.NBT_SEARCH_BOX_MODE)) {
            searchBoxMode = tag.getInt(GridNetworkNode.NBT_SEARCH_BOX_MODE);
        }

        if (tag.contains(GridNetworkNode.NBT_SIZE)) {
            size = tag.getInt(GridNetworkNode.NBT_SIZE);
        }

        if (tag.contains(GridNetworkNode.NBT_TAB_SELECTED)) {
            tabSelected = tag.getInt(GridNetworkNode.NBT_TAB_SELECTED);
        }

        if (tag.contains(GridNetworkNode.NBT_TAB_PAGE)) {
            tabPage = tag.getInt(GridNetworkNode.NBT_TAB_PAGE);
        }

        StackUtils.readItems(disk, 0, tag);
        StackUtils.readItems(filter, 1, tag);

        if (tag.contains(NBT_ENERGY)) {
            energyStorage = createEnergyStorage(tag.getInt(NBT_ENERGY));
        }

        redstoneMode = RedstoneMode.read(tag);

        if (tag.contains(NBT_ITEM_STORAGE_TRACKER_ID)) {
            itemStorageTrackerId = tag.getUniqueId(NBT_ITEM_STORAGE_TRACKER_ID);
        } else {
            if (tag.contains(NBT_STORAGE_TRACKER)) { //TODO: remove next version
                getItemStorageTracker().readFromNbt(tag.getList(NBT_STORAGE_TRACKER, Constants.NBT.TAG_COMPOUND));
            }
        }

        if (tag.contains(NBT_FLUID_STORAGE_TRACKER_ID)) {
            fluidStorageTrackerId = tag.getUniqueId(NBT_FLUID_STORAGE_TRACKER_ID);
        } else {
            if (tag.contains(NBT_FLUID_STORAGE_TRACKER)) { //TODO: remove next version
                getFluidStorageTracker().readFromNbt(tag.getList(NBT_FLUID_STORAGE_TRACKER, Constants.NBT.TAG_COMPOUND));
            }
        }

        if (tag.contains(NBT_ENCHANTMENTS)) {
            enchants = tag.getList(NBT_ENCHANTMENTS, Constants.NBT.TAG_COMPOUND);
        }
    }

    @Override
    public CompoundNBT writeUpdate(CompoundNBT tag) {
        tag.putInt(NBT_TYPE, getServerGridType().ordinal());

        return super.writeUpdate(tag);
    }

    @Override
    public void readUpdate(CompoundNBT tag) {
        super.readUpdate(tag);

        clientGridType = GridType.values()[tag.getInt(NBT_TYPE)];
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyStorageCap.cast();
        }

        return super.getCapability(cap, direction);
    }

    public void onOpened() {
        drainEnergy(RS.SERVER_CONFIG.getPortableGrid().getOpenUsage());
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;

        markDirty();
    }

    @Override
    public AccessType getAccessType() {
        return AccessType.INSERT_EXTRACT;
    }

    @Override
    public void tick() {
        if (loadNextTick) {
            active = isGridActive();
            diskState = getDiskState();
            loadNextTick = false;
        }
    }
}
