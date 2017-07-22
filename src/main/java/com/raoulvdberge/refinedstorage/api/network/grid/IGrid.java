package com.raoulvdberge.refinedstorage.api.network.grid;

import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.raoulvdberge.refinedstorage.api.util.IFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a grid.
 */
public interface IGrid {
    int SORTING_DIRECTION_ASCENDING = 0;
    int SORTING_DIRECTION_DESCENDING = 1;

    int SORTING_TYPE_QUANTITY = 0;
    int SORTING_TYPE_NAME = 1;
    int SORTING_TYPE_ID = 2;

    int SEARCH_BOX_MODE_NORMAL = 0;
    int SEARCH_BOX_MODE_NORMAL_AUTOSELECTED = 1;
    int SEARCH_BOX_MODE_JEI_SYNCHRONIZED = 2;
    int SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED = 3;

    int VIEW_TYPE_NORMAL = 0;
    int VIEW_TYPE_NON_CRAFTABLES = 1;
    int VIEW_TYPE_CRAFTABLES = 2;

    int SIZE_STRETCH = 0;
    int SIZE_SMALL = 1;
    int SIZE_MEDIUM = 2;
    int SIZE_LARGE = 3;

    /**
     * @return the grid type
     */
    GridType getType();

    /**
     * @return the network of this grid, or null if the network is unavailable
     */
    @Nullable
    INetwork getNetwork();

    /**
     * @return the item grid handler of the network of the grid, or null if the network is unavailable
     */
    @Nullable
    default IItemGridHandler getItemHandler() {
        return getNetwork() != null ? getNetwork().getItemGridHandler() : null;
    }

    /**
     * @return the fluid grid handler of the network of the grid, or null if the network is unavailable
     */
    @Nullable
    default IFluidGridHandler getFluidHandler() {
        return getNetwork() != null ? getNetwork().getFluidGridHandler() : null;
    }

    /**
     * @return an unlocalized gui title
     */
    String getGuiTitle();

    /**
     * @return the view type
     */
    int getViewType();

    /**
     * @return the sorting type
     */
    int getSortingType();

    /**
     * @return the sorting direction
     */
    int getSortingDirection();

    /**
     * @return the search box mode
     */
    int getSearchBoxMode();

    /**
     * @return the current tab that is selected
     */
    int getTabSelected();

    /**
     * @return the size mode
     */
    int getSize();

    /**
     * @param type the new view type
     */
    void onViewTypeChanged(int type);

    /**
     * @param type the new sorting type
     */
    void onSortingTypeChanged(int type);

    /**
     * @param direction the new direction
     */
    void onSortingDirectionChanged(int direction);

    /**
     * @param searchBoxMode the new search box mode
     */
    void onSearchBoxModeChanged(int searchBoxMode);

    /**
     * @param size the new size mode
     */
    void onSizeChanged(int size);

    /**
     * @param tab the new selected tab
     */
    void onTabSelectionChanged(int tab);

    /**
     * @return the filters
     */
    List<IFilter> getFilters();

    /**
     * @return the tabs
     */
    List<IGridTab> getTabs();

    /**
     * @return the inventory of the filters
     */
    IItemHandlerModifiable getFilter();

    /**
     * @return the crafting matrix, or null if not a crafting grid
     */
    @Nullable
    InventoryCrafting getCraftingMatrix();

    /**
     * @return the crafting result inventory, or null if not a crafting grid
     */
    @Nullable
    InventoryCraftResult getCraftingResult();

    /**
     * Called when the crafting matrix changes.
     */
    void onCraftingMatrixChanged();

    /**
     * Called when an item is crafted in a crafting grid.
     *
     * @param player the player that crafted the item
     */
    void onCrafted(EntityPlayer player);

    /**
     * Called when an item is crafted with shift click (up to 64 items) in a crafting grid.
     *
     * @param player the player that crafted the item
     */
    void onCraftedShift(EntityPlayer player);

    /**
     * Called when a JEI recipe transfer occurs.
     *
     * @param player the player
     * @param recipe a 9*x array stack array, where x is the possible combinations for the given slot
     */
    void onRecipeTransfer(EntityPlayer player, ItemStack[][] recipe);

    /**
     * Called when the grid is closed.
     *
     * @param player the player
     */
    void onClosed(EntityPlayer player);

    /**
     * @return true if the grid is active, false otherwise
     */
    boolean isActive();

    static boolean isValidViewType(int type) {
        return type == VIEW_TYPE_NORMAL ||
            type == VIEW_TYPE_CRAFTABLES ||
            type == VIEW_TYPE_NON_CRAFTABLES;
    }

    static boolean isValidSearchBoxMode(int mode) {
        return mode == SEARCH_BOX_MODE_NORMAL ||
            mode == SEARCH_BOX_MODE_NORMAL_AUTOSELECTED ||
            mode == SEARCH_BOX_MODE_JEI_SYNCHRONIZED ||
            mode == SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED;
    }

    static boolean isSearchBoxModeWithAutoselection(int mode) {
        return mode == SEARCH_BOX_MODE_NORMAL_AUTOSELECTED || mode == SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED;
    }

    static boolean isValidSortingType(int type) {
        return type == SORTING_TYPE_QUANTITY || type == SORTING_TYPE_NAME || type == SORTING_TYPE_ID;
    }

    static boolean isValidSortingDirection(int direction) {
        return direction == SORTING_DIRECTION_ASCENDING || direction == SORTING_DIRECTION_DESCENDING;
    }

    static boolean isValidSize(int size) {
        return size == SIZE_STRETCH ||
            size == SIZE_SMALL ||
            size == SIZE_MEDIUM ||
            size == SIZE_LARGE;
    }
}
