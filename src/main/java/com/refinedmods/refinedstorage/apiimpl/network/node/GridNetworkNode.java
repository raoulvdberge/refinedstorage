package com.refinedmods.refinedstorage.apiimpl.network.node;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.RSItems;
import com.refinedmods.refinedstorage.api.network.grid.*;
import com.refinedmods.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCache;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCacheListener;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IFilter;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.AllowedTagList;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.FluidGridStorageCacheListener;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.ItemGridStorageCacheListener;
import com.refinedmods.refinedstorage.block.GridBlock;
import com.refinedmods.refinedstorage.block.NetworkNodeBlock;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.inventory.fluid.FluidInventory;
import com.refinedmods.refinedstorage.inventory.item.BaseItemHandler;
import com.refinedmods.refinedstorage.inventory.item.FilterItemHandler;
import com.refinedmods.refinedstorage.inventory.item.validator.ItemValidator;
import com.refinedmods.refinedstorage.inventory.listener.NetworkNodeFluidInventoryListener;
import com.refinedmods.refinedstorage.inventory.listener.NetworkNodeInventoryListener;
import com.refinedmods.refinedstorage.item.PatternItem;
import com.refinedmods.refinedstorage.tile.config.IType;
import com.refinedmods.refinedstorage.tile.data.TileDataManager;
import com.refinedmods.refinedstorage.tile.grid.GridTile;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridNetworkNode extends NetworkNode implements INetworkAwareGrid, IType {
    public static final ResourceLocation ID = new ResourceLocation(RS.ID, "grid");
    public static final ResourceLocation CRAFTING_ID = new ResourceLocation(RS.ID, "crafting_grid");
    public static final ResourceLocation PATTERN_ID = new ResourceLocation(RS.ID, "pattern_grid");
    public static final ResourceLocation FLUID_ID = new ResourceLocation(RS.ID, "fluid_grid");

    public static final String NBT_VIEW_TYPE = "ViewType";
    public static final String NBT_SORTING_DIRECTION = "SortingDirection";
    public static final String NBT_SORTING_TYPE = "SortingType";
    public static final String NBT_SEARCH_BOX_MODE = "SearchBoxMode";
    private static final String NBT_EXACT_MODE = "Exact";
    public static final String NBT_TAB_SELECTED = "TabSelected";
    public static final String NBT_TAB_PAGE = "TabPage";
    public static final String NBT_SIZE = "Size";
    private static final String NBT_PROCESSING_PATTERN = "ProcessingPattern";
    private static final String NBT_PROCESSING_TYPE = "ProcessingType";
    private static final String NBT_PROCESSING_MATRIX_FLUIDS = "ProcessingMatrixFluids";
    private static final String NBT_ALLOWED_TAGS = "AllowedTags";

    private final AllowedTagList allowedTagList = new AllowedTagList(this::updateAllowedTags);

    
    private class DummyContainer extends Container {
        protected DummyContainer() {
            super(ContainerType.CRAFTING, 0);
        }

        public PlayerEntity owner = null;
        
        @Override
        public boolean canInteractWith(PlayerEntity player) {
            return false;
        }

        @Override
        public void onCraftMatrixChanged(IInventory inventory) {
            if (!world.isRemote) {
                onCraftingMatrixChanged();
            }
        }
    }
    
    private final DummyContainer craftingContainer = new DummyContainer();
    
    private ICraftingRecipe currentRecipe;
    private final CraftingInventory matrix = new CraftingInventory(craftingContainer, 3, 3);
    private final CraftResultInventory result = new CraftResultInventory();
    private final BaseItemHandler processingMatrix = new BaseItemHandler(9 * 2)
        .addListener(new NetworkNodeInventoryListener(this))
        .addListener((handler, slot, reading) -> {
            if (!reading && slot < 9) {
                allowedTagList.clearItemTags(slot);
            }
        });
    private final FluidInventory processingMatrixFluids = new FluidInventory(9 * 2, FluidAttributes.BUCKET_VOLUME * 64)
        .addListener(new NetworkNodeFluidInventoryListener(this))
        .addListener((handler, slot, reading) -> {
            if (!reading && slot < 9) {
                allowedTagList.clearFluidTags(slot);
            }
        });

    private boolean readingInventory;

    private final Set<ICraftingGridListener> craftingListeners = new HashSet<>();

    private final BaseItemHandler patterns = new BaseItemHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == 1 ? 1 : super.getSlotLimit(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            // Allow in slot 0
            // Disallow in slot 1
            // Only allow in slot 1 when it isn't a blank pattern
            // This makes it so that written patterns can be re-inserted in slot 1 to be overwritten again
            // This makes it so that blank patterns can't be inserted in slot 1 through hoppers.
            if (slot == 0 || stack.getTag() != null) {
                return super.insertItem(slot, stack, simulate);
            }

            return stack;
        }
    }
        .addValidator(new ItemValidator(RSItems.PATTERN.get()))
        .addListener(new NetworkNodeInventoryListener(this))
        .addListener(((handler, slot, reading) -> {
            ItemStack pattern = handler.getStackInSlot(slot);

            if (!reading && slot == 1 && !pattern.isEmpty()) {
                boolean processing = PatternItem.isProcessing(pattern);

                if (processing) {
                    for (int i = 0; i < 9; ++i) {
                        processingMatrix.setStackInSlot(i, PatternItem.getInputSlot(pattern, i));
                        processingMatrixFluids.setFluid(i, PatternItem.getFluidInputSlot(pattern, i));
                    }

                    for (int i = 0; i < 9; ++i) {
                        processingMatrix.setStackInSlot(9 + i, PatternItem.getOutputSlot(pattern, i));
                        processingMatrixFluids.setFluid(9 + i, PatternItem.getFluidOutputSlot(pattern, i));
                    }

                    AllowedTagList allowedTagsFromPattern = PatternItem.getAllowedTags(pattern);

                    if (allowedTagsFromPattern != null) {
                        allowedTagList.setAllowedItemTags(allowedTagsFromPattern.getAllowedItemTags());
                        allowedTagList.setAllowedFluidTags(allowedTagsFromPattern.getAllowedFluidTags());
                    }
                } else {
                    for (int i = 0; i < 9; ++i) {
                        matrix.setInventorySlotContents(i, PatternItem.getInputSlot(pattern, i));
                    }
                }

                setProcessingPattern(processing);
                markDirty();
            }
        }));

    private final List<IFilter> filters = new ArrayList<>();
    private final List<IGridTab> tabs = new ArrayList<>();
    private final FilterItemHandler filter = (FilterItemHandler) new FilterItemHandler(filters, tabs).addListener(new NetworkNodeInventoryListener(this));

    private final GridType type;

    private int viewType = VIEW_TYPE_NORMAL;
    private int sortingDirection = SORTING_DIRECTION_DESCENDING;
    private int sortingType = SORTING_TYPE_QUANTITY;
    private int searchBoxMode = SEARCH_BOX_MODE_NORMAL;
    private int size = SIZE_STRETCH;

    private int tabSelected = -1;
    private int tabPage = 0;

    private boolean exactPattern = true;
    private boolean processingPattern = false;
    private int processingType = IType.ITEMS;

    public GridNetworkNode(World world, BlockPos pos, GridType type) {
        super(world, pos);

        this.type = type;
    }

    public static ResourceLocation getId(GridType type) {
        switch (type) {
            case NORMAL:
                return ID;
            case CRAFTING:
                return CRAFTING_ID;
            case PATTERN:
                return PATTERN_ID;
            case FLUID:
                return FLUID_ID;
            default:
                throw new IllegalArgumentException("Unknown grid type " + type);
        }
    }

    public AllowedTagList getAllowedTagList() {
        return allowedTagList;
    }

    private void updateAllowedTags() {
        markDirty();

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof GridTile) {
            ((GridTile) tile).getDataManager().sendParameterToWatchers(GridTile.ALLOWED_ITEM_TAGS);
            ((GridTile) tile).getDataManager().sendParameterToWatchers(GridTile.ALLOWED_FLUID_TAGS);
        }
    }

    @Override
    public int getEnergyUsage() {
        switch (type) {
            case NORMAL:
                return RS.SERVER_CONFIG.getGrid().getGridUsage();
            case CRAFTING:
                return RS.SERVER_CONFIG.getGrid().getCraftingGridUsage();
            case PATTERN:
                return RS.SERVER_CONFIG.getGrid().getPatternGridUsage();
            case FLUID:
                return RS.SERVER_CONFIG.getGrid().getFluidGridUsage();
            default:
                return 0;
        }
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public void setSortingDirection(int sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void setSortingType(int sortingType) {
        this.sortingType = sortingType;
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

    public boolean isExactPattern() {
        return exactPattern;
    }

    public void setExactPattern(boolean exactPattern) {
        this.exactPattern = exactPattern;
    }

    public boolean isProcessingPattern() {
        return world.isRemote ? GridTile.PROCESSING_PATTERN.getValue() : processingPattern;
    }

    public void setProcessingPattern(boolean processingPattern) {
        this.processingPattern = processingPattern;
    }

    @Override
    public GridType getGridType() {
        return type;
    }

    @Override
    public IStorageCacheListener createListener(ServerPlayerEntity player) {
        return type == GridType.FLUID ? new FluidGridStorageCacheListener(player, network) : new ItemGridStorageCacheListener(player, network);
    }

    @Nullable
    @Override
    public IStorageCache getStorageCache() {
        if (network != null) {
            return type == GridType.FLUID ? network.getFluidStorageCache() : network.getItemStorageCache();
        }

        return null;
    }

    @Nullable
    @Override
    public IItemGridHandler getItemHandler() {
        return network != null ? network.getItemGridHandler() : null;
    }

    @Nullable
    @Override
    public IFluidGridHandler getFluidHandler() {
        return network != null ? network.getFluidGridHandler() : null;
    }

    @Override
    public void addCraftingListener(ICraftingGridListener listener) {
        
        if(listener instanceof GridContainer) {
            this.craftingContainer.owner = ((GridContainer) listener).getPlayer();
        }
        craftingListeners.add(listener);
    }

    @Override
    public void removeCraftingListener(ICraftingGridListener listener) {
        craftingListeners.remove(listener);
    }

    @Override
    public ITextComponent getTitle() {
        switch (type) {
            case CRAFTING:
                return new TranslationTextComponent("gui.refinedstorage.crafting_grid");
            case PATTERN:
                return new TranslationTextComponent("gui.refinedstorage.pattern_grid");
            case FLUID:
                return new TranslationTextComponent("gui.refinedstorage.fluid_grid");
            default:
                return new TranslationTextComponent("gui.refinedstorage.grid");
        }
    }

    public IItemHandler getPatterns() {
        return patterns;
    }

    @Override
    public IItemHandlerModifiable getFilter() {
        return filter;
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
    public CraftingInventory getCraftingMatrix() {
        return matrix;
    }

    @Override
    public CraftResultInventory getCraftingResult() {
        return result;
    }

    public BaseItemHandler getProcessingMatrix() {
        return processingMatrix;
    }

    public FluidInventory getProcessingMatrixFluids() {
        return processingMatrixFluids;
    }

    @Override
    public void onCraftingMatrixChanged() {
        if (currentRecipe == null || !currentRecipe.matches(matrix, world)) {
            currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, matrix, world).orElse(null);
        }

        if (currentRecipe == null) {
            result.setInventorySlotContents(0, ItemStack.EMPTY);
        } else {
            result.setInventorySlotContents(0, currentRecipe.getCraftingResult(matrix));
        }

        craftingListeners.forEach(ICraftingGridListener::onCraftingMatrixChanged);

        if (!readingInventory) {
            markDirty();
        }
    }

    @Override
    public void onRecipeTransfer(PlayerEntity player, ItemStack[][] recipe) {
        craftingContainer.owner = player;
        API.instance().getCraftingGridBehavior().onRecipeTransfer(this, player, recipe);
    }

    public void clearMatrix() {
        for (int i = 0; i < processingMatrix.getSlots(); ++i) {
            processingMatrix.setStackInSlot(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < processingMatrixFluids.getSlots(); ++i) {
            processingMatrixFluids.setFluid(i, FluidStack.EMPTY);
        }

        for (int i = 0; i < matrix.getSizeInventory(); ++i) {
            matrix.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        // NO OP
    }

    @Override
    public boolean isGridActive() {
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof GridBlock) {
            return state.get(NetworkNodeBlock.CONNECTED);
        }

        return false;
    }

    @Override
    public int getSlotId() {
        return -1;
    }

    @Override
    public void onCrafted(PlayerEntity player, @Nullable IStackList<ItemStack> availableItems, @Nullable IStackList<ItemStack> usedItems) {
        craftingContainer.owner = player;
        API.instance().getCraftingGridBehavior().onCrafted(this, currentRecipe, player, availableItems, usedItems);
    }

    @Override
    public void onClear(PlayerEntity player) {
        craftingContainer.owner = player;
        if (type == GridType.CRAFTING) {
            if (network != null && network.canRun() && network.getSecurityManager().hasPermission(Permission.INSERT, player)) {
                for (int i = 0; i < matrix.getSizeInventory(); ++i) {
                    ItemStack slot = matrix.getStackInSlot(i);

                    if (!slot.isEmpty()) {
                        matrix.setInventorySlotContents(i, network.insertItem(slot, slot.getCount(), Action.PERFORM));

                        network.getItemStorageTracker().changed(player, slot.copy());
                    }
                }
            } else {
                for (int i = 0; i < matrix.getSizeInventory(); i++) {
                    ItemStack slot = matrix.getStackInSlot(i);

                    if (!slot.isEmpty()) {
                        player.inventory.addItemStackToInventory(matrix.getStackInSlot(i));
                    }

                    onCraftingMatrixChanged();
                }
            }
        } else if (type == GridType.PATTERN) {
            clearMatrix();
        }

    }

    @Override
    public void onCraftedShift(PlayerEntity player) {
        craftingContainer.owner = player;
        API.instance().getCraftingGridBehavior().onCraftedShift(this, player);
    }

    public void onCreatePattern(ServerPlayerEntity player) {
        craftingContainer.owner = player;
        if (canCreatePattern()) {
            if (patterns.getStackInSlot(1).isEmpty()) {
                patterns.extractItem(0, 1, false);
            }

            ItemStack pattern = new ItemStack(RSItems.PATTERN.get());

            PatternItem.setToCurrentVersion(pattern);
            PatternItem.setProcessing(pattern, processingPattern);
            PatternItem.setPatternCreator(pattern, player.getGameProfile().getId());
            
            if (!processingPattern) {
                PatternItem.setExact(pattern, exactPattern);
            } else {
                PatternItem.setAllowedTags(pattern, allowedTagList);
            }

            if (processingPattern) {
                for (int i = 0; i < 18; ++i) {
                    if (!processingMatrix.getStackInSlot(i).isEmpty()) {
                        if (i >= 9) {
                            PatternItem.setOutputSlot(pattern, i - 9, processingMatrix.getStackInSlot(i));
                        } else {
                            PatternItem.setInputSlot(pattern, i, processingMatrix.getStackInSlot(i));
                        }
                    }

                    FluidStack fluid = processingMatrixFluids.getFluid(i);
                    if (!fluid.isEmpty()) {
                        if (i >= 9) {
                            PatternItem.setFluidOutputSlot(pattern, i - 9, fluid);
                        } else {
                            PatternItem.setFluidInputSlot(pattern, i, fluid);
                        }
                    }
                }
            } else {
                for (int i = 0; i < 9; ++i) {
                    ItemStack ingredient = matrix.getStackInSlot(i);

                    if (!ingredient.isEmpty()) {
                        PatternItem.setInputSlot(pattern, i, ingredient);
                    }
                }
            }

            patterns.setStackInSlot(1, pattern);
        }
    }

    private boolean isPatternAvailable() {
        return !(patterns.getStackInSlot(0).isEmpty() && patterns.getStackInSlot(1).isEmpty());
    }

    public boolean canCreatePattern() {
        if (!isPatternAvailable()) {
            return false;
        }

        if (isProcessingPattern()) {
            int inputsFilled = 0;
            int outputsFilled = 0;

            for (int i = 0; i < 9; ++i) {
                if (!processingMatrix.getStackInSlot(i).isEmpty()) {
                    inputsFilled++;
                }

                if (!processingMatrixFluids.getFluid(i).isEmpty()) {
                    inputsFilled++;
                }
            }

            for (int i = 9; i < 18; ++i) {
                if (!processingMatrix.getStackInSlot(i).isEmpty()) {
                    outputsFilled++;
                }

                if (!processingMatrixFluids.getFluid(i).isEmpty()) {
                    outputsFilled++;
                }
            }

            return inputsFilled > 0 && outputsFilled > 0;
        } else {
            return !result.getStackInSlot(0).isEmpty() && isPatternAvailable();
        }
    }

    @Override
    public int getViewType() {
        return world.isRemote ? GridTile.VIEW_TYPE.getValue() : viewType;
    }

    @Override
    public int getSortingDirection() {
        return world.isRemote ? GridTile.SORTING_DIRECTION.getValue() : sortingDirection;
    }

    @Override
    public int getSortingType() {
        return world.isRemote ? GridTile.SORTING_TYPE.getValue() : sortingType;
    }

    @Override
    public int getSearchBoxMode() {
        return world.isRemote ? GridTile.SEARCH_BOX_MODE.getValue() : searchBoxMode;
    }

    @Override
    public int getSize() {
        return world.isRemote ? GridTile.SIZE.getValue() : size;
    }

    @Override
    public int getTabSelected() {
        return world.isRemote ? GridTile.TAB_SELECTED.getValue() : tabSelected;
    }

    @Override
    public int getTabPage() {
        return world.isRemote ? GridTile.TAB_PAGE.getValue() : Math.min(tabPage, getTotalTabPages());
    }

    @Override
    public int getTotalTabPages() {
        return (int) Math.floor((float) Math.max(0, tabs.size() - 1) / (float) IGrid.TABS_PER_PAGE);
    }

    @Override
    public void onViewTypeChanged(int type) {
        TileDataManager.setParameter(GridTile.VIEW_TYPE, type);
    }

    @Override
    public void onSortingTypeChanged(int type) {
        TileDataManager.setParameter(GridTile.SORTING_TYPE, type);
    }

    @Override
    public void onSortingDirectionChanged(int direction) {
        TileDataManager.setParameter(GridTile.SORTING_DIRECTION, direction);
    }

    @Override
    public void onSearchBoxModeChanged(int searchBoxMode) {
        TileDataManager.setParameter(GridTile.SEARCH_BOX_MODE, searchBoxMode);
    }

    @Override
    public void onSizeChanged(int size) {
        TileDataManager.setParameter(GridTile.SIZE, size);
    }

    @Override
    public void onTabSelectionChanged(int tab) {
        TileDataManager.setParameter(GridTile.TAB_SELECTED, tab);
    }

    @Override
    public void onTabPageChanged(int page) {
        if (page >= 0 && page <= getTotalTabPages()) {
            TileDataManager.setParameter(GridTile.TAB_PAGE, page);
        }
    }

    @Override
    public int getType() {
        return world.isRemote ? GridTile.PROCESSING_TYPE.getValue() : processingType;
    }

    @Override
    public void setType(int type) {
        this.processingType = type;

        this.markDirty();
    }

    @Override
    public IItemHandlerModifiable getItemFilters() {
        return getProcessingMatrix();
    }

    @Override
    public FluidInventory getFluidFilters() {
        return getProcessingMatrixFluids();
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        if (tag.contains(NBT_ALLOWED_TAGS)) {
            allowedTagList.readFromNbt(tag.getCompound(NBT_ALLOWED_TAGS));
        }

        readingInventory = true;

        StackUtils.readItems(matrix, 0, tag);
        StackUtils.readItems(patterns, 1, tag);
        StackUtils.readItems(filter, 2, tag);
        StackUtils.readItems(processingMatrix, 3, tag);

        if (tag.contains(NBT_PROCESSING_MATRIX_FLUIDS)) {
            processingMatrixFluids.readFromNbt(tag.getCompound(NBT_PROCESSING_MATRIX_FLUIDS));
        }

        if (tag.contains(NBT_TAB_SELECTED)) {
            tabSelected = tag.getInt(NBT_TAB_SELECTED);
        }

        if (tag.contains(NBT_TAB_PAGE)) {
            tabPage = tag.getInt(NBT_TAB_PAGE);
        }

        readingInventory = false;
    }

    @Override
    public ResourceLocation getId() {
        return getId(type);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put(NBT_ALLOWED_TAGS, allowedTagList.writeToNbt());

        StackUtils.writeItems(matrix, 0, tag);
        StackUtils.writeItems(patterns, 1, tag);
        StackUtils.writeItems(filter, 2, tag);
        StackUtils.writeItems(processingMatrix, 3, tag);

        tag.put(NBT_PROCESSING_MATRIX_FLUIDS, processingMatrixFluids.writeToNbt());
        tag.putInt(NBT_TAB_SELECTED, tabSelected);
        tag.putInt(NBT_TAB_PAGE, tabPage);

        return tag;
    }

    @Override
    public CompoundNBT writeConfiguration(CompoundNBT tag) {
        super.writeConfiguration(tag);

        tag.putInt(NBT_VIEW_TYPE, viewType);
        tag.putInt(NBT_SORTING_DIRECTION, sortingDirection);
        tag.putInt(NBT_SORTING_TYPE, sortingType);
        tag.putInt(NBT_SEARCH_BOX_MODE, searchBoxMode);
        tag.putInt(NBT_SIZE, size);

        tag.putBoolean(NBT_EXACT_MODE, exactPattern);
        tag.putBoolean(NBT_PROCESSING_PATTERN, processingPattern);
        tag.putInt(NBT_PROCESSING_TYPE, processingType);

        return tag;
    }

    @Override
    public void readConfiguration(CompoundNBT tag) {
        super.readConfiguration(tag);

        if (tag.contains(NBT_VIEW_TYPE)) {
            viewType = tag.getInt(NBT_VIEW_TYPE);
        }

        if (tag.contains(NBT_SORTING_DIRECTION)) {
            sortingDirection = tag.getInt(NBT_SORTING_DIRECTION);
        }

        if (tag.contains(NBT_SORTING_TYPE)) {
            sortingType = tag.getInt(NBT_SORTING_TYPE);
        }

        if (tag.contains(NBT_SEARCH_BOX_MODE)) {
            searchBoxMode = tag.getInt(NBT_SEARCH_BOX_MODE);
        }

        if (tag.contains(NBT_SIZE)) {
            size = tag.getInt(NBT_SIZE);
        }

        if (tag.contains(NBT_EXACT_MODE)) {
            exactPattern = tag.getBoolean(NBT_EXACT_MODE);
        }

        if (tag.contains(NBT_PROCESSING_PATTERN)) {
            processingPattern = tag.getBoolean(NBT_PROCESSING_PATTERN);
        }

        if (tag.contains(NBT_PROCESSING_TYPE)) {
            processingType = tag.getInt(NBT_PROCESSING_TYPE);
        }
    }

    @Override
    public IItemHandler getDrops() {
        switch (type) {
            case CRAFTING:
                return new CombinedInvWrapper(filter, new InvWrapper(matrix));
            case PATTERN:
                return new CombinedInvWrapper(filter, patterns);
            default:
                return new CombinedInvWrapper(filter);
        }
    }
}
