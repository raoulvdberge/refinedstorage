package com.raoulvdberge.refinedstorage.apiimpl.network.node;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.grid.GridType;
import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.api.network.grid.IGridNetworkAware;
import com.raoulvdberge.refinedstorage.api.network.grid.IGridTab;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItem;
import com.raoulvdberge.refinedstorage.api.network.item.NetworkItemAction;
import com.raoulvdberge.refinedstorage.api.network.security.Permission;
import com.raoulvdberge.refinedstorage.api.storage.IStorageCache;
import com.raoulvdberge.refinedstorage.api.storage.IStorageCacheListener;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.api.util.IFilter;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageCacheListenerGridFluid;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageCacheListenerGridItem;
import com.raoulvdberge.refinedstorage.block.BlockGrid;
import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBase;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerFilter;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerListenerNetworkNode;
import com.raoulvdberge.refinedstorage.inventory.ItemValidatorBasic;
import com.raoulvdberge.refinedstorage.item.ItemPattern;
import com.raoulvdberge.refinedstorage.tile.data.TileDataManager;
import com.raoulvdberge.refinedstorage.tile.grid.TileGrid;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NetworkNodeGrid extends NetworkNode implements IGridNetworkAware {
    public static final String ID = "grid";

    public static final String NBT_VIEW_TYPE = "ViewType";
    public static final String NBT_SORTING_DIRECTION = "SortingDirection";
    public static final String NBT_SORTING_TYPE = "SortingType";
    public static final String NBT_SEARCH_BOX_MODE = "SearchBoxMode";
    public static final String NBT_OREDICT_PATTERN = "OredictPattern";
    public static final String NBT_TAB_SELECTED = "TabSelected";
    public static final String NBT_TAB_PAGE = "TabPage";
    public static final String NBT_SIZE = "Size";
    public static final String NBT_PROCESSING_PATTERN = "ProcessingPattern";

    private Container craftingContainer = new Container() {
        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return false;
        }

        @Override
        public void onCraftMatrixChanged(IInventory inventory) {
            onCraftingMatrixChanged();
        }
    };
    private IRecipe currentRecipe;
    private InventoryCrafting matrix = new InventoryCrafting(craftingContainer, 3, 3);
    private InventoryCraftResult result = new InventoryCraftResult();
    private ItemHandlerBase matrixProcessing = new ItemHandlerBase(9 * 2, new ItemHandlerListenerNetworkNode(this));

    private ItemHandlerBase patterns = new ItemHandlerBase(2, new ItemHandlerListenerNetworkNode(this), new ItemValidatorBasic(RSItems.PATTERN)) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            ItemStack pattern = getStackInSlot(slot);
            if (slot == 1 && !pattern.isEmpty()) {
                boolean isPatternProcessing = ItemPattern.isProcessing(pattern);

                if (isPatternProcessing && processingPattern) {
                    for (int i = 0; i < 9; ++i) {
                        matrixProcessing.setStackInSlot(i, StackUtils.nullToEmpty(ItemPattern.getInputSlot(pattern, i)));
                    }

                    for (int i = 0; i < 9; ++i) {
                        matrixProcessing.setStackInSlot(9 + i, StackUtils.nullToEmpty(ItemPattern.getOutputSlot(pattern, i)));
                    }
                } else if (!isPatternProcessing && !processingPattern) {
                    for (int i = 0; i < 9; ++i) {
                        matrix.setInventorySlotContents(i, StackUtils.nullToEmpty(ItemPattern.getInputSlot(pattern, i)));
                    }
                }

                sendSlotUpdate(false);
            }
        }

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
            if (slot == 0 || stack.getTagCompound() != null) {
                return super.insertItem(slot, stack, simulate);
            }

            return stack;
        }
    };
    private List<IFilter> filters = new ArrayList<>();
    private List<IGridTab> tabs = new ArrayList<>();
    private ItemHandlerFilter filter = new ItemHandlerFilter(filters, tabs, new ItemHandlerListenerNetworkNode(this));

    private GridType type;

    private int viewType = VIEW_TYPE_NORMAL;
    private int sortingDirection = SORTING_DIRECTION_DESCENDING;
    private int sortingType = SORTING_TYPE_QUANTITY;
    private int searchBoxMode = SEARCH_BOX_MODE_NORMAL;
    private int size = SIZE_STRETCH;

    private int tabSelected = -1;
    private int tabPage = 0;

    private boolean oredictPattern = false;
    private boolean processingPattern = false;

    public NetworkNodeGrid(World world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public int getEnergyUsage() {
        switch (getType()) {
            case NORMAL:
                return RS.INSTANCE.config.gridUsage;
            case CRAFTING:
                return RS.INSTANCE.config.craftingGridUsage;
            case PATTERN:
                return RS.INSTANCE.config.patternGridUsage;
            case FLUID:
                return RS.INSTANCE.config.fluidGridUsage;
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

    public boolean isOredictPattern() {
        return oredictPattern;
    }

    public void setOredictPattern(boolean oredictPattern) {
        this.oredictPattern = oredictPattern;
    }

    public boolean isProcessingPattern() {
        return world.isRemote ? TileGrid.PROCESSING_PATTERN.getValue() : processingPattern;
    }

    public void setProcessingPattern(boolean processingPattern) {
        this.processingPattern = processingPattern;
    }

    public GridType getType() {
        if (type == null && world.getBlockState(pos).getBlock() == RSBlocks.GRID) {
            type = (GridType) world.getBlockState(pos).getValue(BlockGrid.TYPE);
        }

        return type == null ? GridType.NORMAL : type;
    }

    @Override
    public IStorageCacheListener createListener(EntityPlayerMP player) {
        return getType() == GridType.FLUID ? new StorageCacheListenerGridFluid(player, network) : new StorageCacheListenerGridItem(player, network);
    }

    @Nullable
    @Override
    public IStorageCache getStorageCache() {
        return network != null ? (getType() == GridType.FLUID ? network.getFluidStorageCache() : network.getItemStorageCache()) : null;
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
    public String getGuiTitle() {
        GridType type = getType();

        switch (type) {
            case CRAFTING:
                return "gui.refinedstorage:crafting_grid";
            case PATTERN:
                return "gui.refinedstorage:pattern_grid";
            case FLUID:
                return "gui.refinedstorage:fluid_grid";
            default:
                return "gui.refinedstorage:grid";
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
    public InventoryCrafting getCraftingMatrix() {
        return matrix;
    }

    @Override
    public InventoryCraftResult getCraftingResult() {
        return result;
    }

    public ItemHandlerBase getProcessingMatrix() {
        return matrixProcessing;
    }

    @Override
    public void onCraftingMatrixChanged() {
        if (currentRecipe == null || !currentRecipe.matches(matrix, world)) {
            currentRecipe = CraftingManager.findMatchingRecipe(matrix, world);
        }

        if (currentRecipe == null) {
            result.setInventorySlotContents(0, ItemStack.EMPTY);
        } else {
            result.setInventorySlotContents(0, currentRecipe.getCraftingResult(matrix));
        }

        markDirty();
    }

    @Override
    public void onRecipeTransfer(EntityPlayer player, ItemStack[][] recipe) {
        onRecipeTransfer(this, player, recipe);
    }

    public static void onRecipeTransfer(IGridNetworkAware grid, EntityPlayer player, ItemStack[][] recipe) {
        INetwork network = grid.getNetwork();

        if (network != null && grid.getType() == GridType.CRAFTING && !network.getSecurityManager().hasPermission(Permission.EXTRACT, player)) {
            return;
        }

        // First try to empty the crafting matrix
        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            ItemStack slot = grid.getCraftingMatrix().getStackInSlot(i);

            if (!slot.isEmpty()) {
                // Only if we are a crafting grid. Pattern grids can just be emptied.
                if (grid.getType() == GridType.CRAFTING) {
                    // If we are connected, try to insert into network. If it fails, stop.
                    if (network != null) {
                        if (network.insertItem(slot, slot.getCount(), true) != null) {
                            return;
                        } else {
                            network.insertItem(slot, slot.getCount(), false);
                        }
                    } else {
                        // If we aren't connected, try to insert into player inventory. If it fails, stop.
                        if (!player.inventory.addItemStackToInventory(slot.copy())) {
                            return;
                        }
                    }
                }

                grid.getCraftingMatrix().setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }

        // Now let's fill the matrix
        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            if (recipe[i] != null) {
                ItemStack[] possibilities = recipe[i];

                // If we are a crafting grid
                if (grid.getType() == GridType.CRAFTING) {
                    boolean found = false;

                    // If we are connected, first try to get the possibilities from the network
                    if (network != null) {
                        for (ItemStack possibility : possibilities) {
                            ItemStack took = network.extractItem(possibility, 1, IComparer.COMPARE_NBT | (possibility.getItem().isDamageable() ? 0 : IComparer.COMPARE_DAMAGE), false);

                            if (took != null) {
                                grid.getCraftingMatrix().setInventorySlotContents(i, StackUtils.nullToEmpty(took));

                                found = true;

                                break;
                            }
                        }
                    }

                    // If we haven't found anything in the network (or we are disconnected), go look in the player inventory
                    if (!found) {
                        for (ItemStack possibility : possibilities) {
                            for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
                                if (API.instance().getComparer().isEqual(possibility, player.inventory.getStackInSlot(j), IComparer.COMPARE_NBT | (possibility.getItem().isDamageable() ? 0 : IComparer.COMPARE_DAMAGE))) {
                                    grid.getCraftingMatrix().setInventorySlotContents(i, ItemHandlerHelper.copyStackWithSize(player.inventory.getStackInSlot(j), 1));

                                    player.inventory.decrStackSize(j, 1);

                                    found = true;

                                    break;
                                }
                            }

                            if (found) {
                                break;
                            }
                        }
                    }
                } else if (grid.getType() == GridType.PATTERN) {
                    // If we are a pattern grid we can just set the slot
                    grid.getCraftingMatrix().setInventorySlotContents(i, possibilities.length == 0 ? ItemStack.EMPTY : possibilities[0]);
                }
            }
        }
    }

    public void clearMatrix() {
        for (int i = 0; i < matrixProcessing.getSlots(); ++i) {
            matrixProcessing.setStackInSlot(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < matrix.getSizeInventory(); ++i) {
            matrix.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    public void sendSlotUpdate(boolean initSlots) {
        if (!world.isRemote) {
            world.getMinecraftServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(player -> player.openContainer instanceof ContainerGrid && ((ContainerGrid) player.openContainer).getTile() != null && ((ContainerGrid) player.openContainer).getTile().getPos().equals(pos))
                .forEach(player -> {
                    if (initSlots) {
                        ((ContainerGrid) player.openContainer).initSlots();
                    }

                    ((ContainerGrid) player.openContainer).sendAllSlots();
                });
        }
    }

    @Override
    public void onClosed(EntityPlayer player) {
        // NO OP
    }

    @Override
    public void onCrafted(EntityPlayer player) {
        onCrafted(this, world, player);
    }

    public static void onCrafted(IGridNetworkAware grid, World world, EntityPlayer player) {
        NonNullList<ItemStack> remainder = CraftingManager.getRemainingItems(grid.getCraftingMatrix(), world);

        INetwork network = grid.getNetwork();

        InventoryCrafting matrix = grid.getCraftingMatrix();

        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            ItemStack slot = matrix.getStackInSlot(i);

            if (i < remainder.size() && !remainder.get(i).isEmpty()) {
                // If there is no space for the remainder, dump it in the player inventory
                if (!slot.isEmpty() && slot.getCount() > 1) {
                    if (!player.inventory.addItemStackToInventory(remainder.get(i).copy())) {
                        ItemStack remainderStack = network == null ? remainder.get(i).copy() : network.insertItem(remainder.get(i).copy(), remainder.get(i).getCount(), false);

                        if (remainderStack != null) {
                            InventoryHelper.spawnItemStack(player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), remainderStack);
                        }
                    }

                    matrix.decrStackSize(i, 1);
                } else {
                    matrix.setInventorySlotContents(i, remainder.get(i).copy());
                }
            } else if (!slot.isEmpty()) {
                if (slot.getCount() == 1 && network != null) {
                    matrix.setInventorySlotContents(i, StackUtils.nullToEmpty(network.extractItem(slot, 1, false)));
                } else {
                    matrix.decrStackSize(i, 1);
                }
            }
        }

        grid.onCraftingMatrixChanged();

        if (network != null) {
            INetworkItem networkItem = network.getNetworkItemHandler().getItem(player);

            if (networkItem != null) {
                networkItem.onAction(NetworkItemAction.ITEM_CRAFTED);
            }
        }
    }

    @Override
    public void onCraftedShift(EntityPlayer player) {
        onCraftedShift(this, player);
    }

    public static void onCraftedShift(IGridNetworkAware grid, EntityPlayer player) {
        List<ItemStack> craftedItemsList = new ArrayList<>();
        int craftedItems = 0;
        ItemStack crafted = grid.getCraftingResult().getStackInSlot(0);

        while (true) {
            grid.onCrafted(player);

            craftedItemsList.add(crafted.copy());

            craftedItems += crafted.getCount();

            if (!API.instance().getComparer().isEqual(crafted, grid.getCraftingResult().getStackInSlot(0)) || craftedItems + crafted.getCount() > crafted.getMaxStackSize()) {
                break;
            }
        }

        INetwork network = grid.getNetwork();

        for (ItemStack craftedItem : craftedItemsList) {
            if (!player.inventory.addItemStackToInventory(craftedItem.copy())) {
                ItemStack remainder = network == null ? craftedItem : network.insertItem(craftedItem, craftedItem.getCount(), false);

                if (remainder != null) {
                    InventoryHelper.spawnItemStack(player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), remainder);
                }
            }
        }

        FMLCommonHandler.instance().firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, craftedItems), grid.getCraftingMatrix());
    }

    public void onCreatePattern() {
        if (canCreatePattern()) {
            if (patterns.getStackInSlot(1).isEmpty()) {
                patterns.extractItem(0, 1, false);
            }

            ItemStack pattern = new ItemStack(RSItems.PATTERN);

            ItemPattern.setOredict(pattern, oredictPattern);
            ItemPattern.setProcessing(pattern, processingPattern);

            if (processingPattern) {
                for (int i = 0; i < 18; ++i) {
                    if (!matrixProcessing.getStackInSlot(i).isEmpty()) {
                        if (i >= 9) {
                            ItemPattern.setOutputSlot(pattern, i - 9, matrixProcessing.getStackInSlot(i));
                        } else {
                            ItemPattern.setInputSlot(pattern, i, matrixProcessing.getStackInSlot(i));
                        }
                    }
                }
            } else {
                for (int i = 0; i < 9; ++i) {
                    ItemStack ingredient = matrix.getStackInSlot(i);

                    if (!ingredient.isEmpty()) {
                        ItemPattern.setInputSlot(pattern, i, ingredient);
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
                if (!matrixProcessing.getStackInSlot(i).isEmpty()) {
                    inputsFilled++;
                }
            }

            for (int i = 9; i < 18; ++i) {
                if (!matrixProcessing.getStackInSlot(i).isEmpty()) {
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
        return world.isRemote ? TileGrid.VIEW_TYPE.getValue() : viewType;
    }

    @Override
    public int getSortingDirection() {
        return world.isRemote ? TileGrid.SORTING_DIRECTION.getValue() : sortingDirection;
    }

    @Override
    public int getSortingType() {
        return world.isRemote ? TileGrid.SORTING_TYPE.getValue() : sortingType;
    }

    @Override
    public int getSearchBoxMode() {
        return world.isRemote ? TileGrid.SEARCH_BOX_MODE.getValue() : searchBoxMode;
    }

    @Override
    public int getSize() {
        return world.isRemote ? TileGrid.SIZE.getValue() : size;
    }

    @Override
    public int getTabSelected() {
        return world.isRemote ? TileGrid.TAB_SELECTED.getValue() : tabSelected;
    }

    @Override
    public int getTabPage() {
        return world.isRemote ? TileGrid.TAB_PAGE.getValue() : Math.min(tabPage, getTotalTabPages());
    }

    @Override
    public int getTotalTabPages() {
        return (int) Math.floor((float) Math.max(0, tabs.size() - 1) / (float) IGrid.TABS_PER_PAGE);
    }

    @Override
    public void onViewTypeChanged(int type) {
        TileDataManager.setParameter(TileGrid.VIEW_TYPE, type);
    }

    @Override
    public void onSortingTypeChanged(int type) {
        TileDataManager.setParameter(TileGrid.SORTING_TYPE, type);
    }

    @Override
    public void onSortingDirectionChanged(int direction) {
        TileDataManager.setParameter(TileGrid.SORTING_DIRECTION, direction);
    }

    @Override
    public void onSearchBoxModeChanged(int searchBoxMode) {
        TileDataManager.setParameter(TileGrid.SEARCH_BOX_MODE, searchBoxMode);
    }

    @Override
    public void onSizeChanged(int size) {
        TileDataManager.setParameter(TileGrid.SIZE, size);
    }

    @Override
    public void onTabSelectionChanged(int tab) {
        TileDataManager.setParameter(TileGrid.TAB_SELECTED, tab);
    }

    @Override
    public void onTabPageChanged(int page) {
        if (page >= 0 && page <= getTotalTabPages()) {
            TileDataManager.setParameter(TileGrid.TAB_PAGE, page);
        }
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        StackUtils.readItems(matrix, 0, tag);
        StackUtils.readItems(patterns, 1, tag);
        StackUtils.readItems(filter, 2, tag);
        StackUtils.readItems(matrixProcessing, 3, tag);

        if (tag.hasKey(NBT_TAB_SELECTED)) {
            tabSelected = tag.getInteger(NBT_TAB_SELECTED);
        }

        if (tag.hasKey(NBT_TAB_PAGE)) {
            tabPage = tag.getInteger(NBT_TAB_PAGE);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        StackUtils.writeItems(matrix, 0, tag);
        StackUtils.writeItems(patterns, 1, tag);
        StackUtils.writeItems(filter, 2, tag);
        StackUtils.writeItems(matrixProcessing, 3, tag);

        tag.setInteger(NBT_TAB_SELECTED, tabSelected);
        tag.setInteger(NBT_TAB_PAGE, tabPage);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        tag.setInteger(NBT_VIEW_TYPE, viewType);
        tag.setInteger(NBT_SORTING_DIRECTION, sortingDirection);
        tag.setInteger(NBT_SORTING_TYPE, sortingType);
        tag.setInteger(NBT_SEARCH_BOX_MODE, searchBoxMode);
        tag.setInteger(NBT_SIZE, size);

        tag.setBoolean(NBT_OREDICT_PATTERN, oredictPattern);
        tag.setBoolean(NBT_PROCESSING_PATTERN, processingPattern);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        if (tag.hasKey(NBT_VIEW_TYPE)) {
            viewType = tag.getInteger(NBT_VIEW_TYPE);
        }

        if (tag.hasKey(NBT_SORTING_DIRECTION)) {
            sortingDirection = tag.getInteger(NBT_SORTING_DIRECTION);
        }

        if (tag.hasKey(NBT_SORTING_TYPE)) {
            sortingType = tag.getInteger(NBT_SORTING_TYPE);
        }

        if (tag.hasKey(NBT_SEARCH_BOX_MODE)) {
            searchBoxMode = tag.getInteger(NBT_SEARCH_BOX_MODE);
        }

        if (tag.hasKey(NBT_SIZE)) {
            size = tag.getInteger(NBT_SIZE);
        }

        if (tag.hasKey(NBT_OREDICT_PATTERN)) {
            oredictPattern = tag.getBoolean(NBT_OREDICT_PATTERN);
        }

        if (tag.hasKey(NBT_PROCESSING_PATTERN)) {
            processingPattern = tag.getBoolean(NBT_PROCESSING_PATTERN);
        }
    }

    @Override
    public IItemHandler getDrops() {
        switch (getType()) {
            case CRAFTING:
                return new CombinedInvWrapper(filter, new InvWrapper(matrix));
            case PATTERN:
                return new CombinedInvWrapper(filter, patterns);
            default:
                return new CombinedInvWrapper(filter);
        }
    }
}
