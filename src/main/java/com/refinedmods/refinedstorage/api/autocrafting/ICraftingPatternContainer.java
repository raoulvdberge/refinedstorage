package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a network node that contains crafting patterns.
 */
public interface ICraftingPatternContainer {
    /**
     * Returns the interval of when a crafting step with a pattern in this container can update.
     * Minimum value is 0 (each tick).
     * <p>
     * Note: rather than maxing out the update interval, implementors might want to balance around {@link #getMaximumSuccessfulCraftingUpdates()}.
     * This method merely speeds up the update rate, it might be more interesting to increase the output rate in {@link #getMaximumSuccessfulCraftingUpdates()}.
     *
     * @return the update interval
     */
    default int getUpdateInterval() {
        return 10;
    }

    /**
     * Returns the amount of successful crafting updates that this container can have per crafting step update.
     * If this limit is reached, crafting patterns from this container won't be able to update until the next
     * eligible crafting step update interval from {@link #getUpdateInterval()}.
     *
     * @return the maximum amount of successful crafting updates
     */
    default int getMaximumSuccessfulCraftingUpdates() {
        return 1;
    }

    /**
     * @return the inventory that this container is connected to, or null if no inventory is present
     */
    @Nullable
    IItemHandler getConnectedInventory();

    /**
     * @return the fluid inventory that this container is connected to, or null if no fluid inventory is present
     */
    @Nullable
    IFluidHandler getConnectedFluidInventory();

    /**
     * @return the tile that this container is connected to, or null if no tile is present
     */
    @Nullable
    TileEntity getConnectedTile();

    /**
     * @return the tile that this container is facing
     */
    TileEntity getFacingTile();

    /**
     * @return the direction to the facing tile
     */
    Direction getDirection();

    /**
     * @return the patterns stored in this container
     */
    List<ICraftingPattern> getPatterns();

    /**
     * @return the pattern inventory, or null if no pattern inventory is present
     */
    @Nullable
    IItemHandlerModifiable getPatternInventory();

    /**
     * The name of this container for categorizing in the Crafting Manager GUI.
     *
     * @return the name of this container
     */
    ITextComponent getName();

    /**
     * @return the position of this container
     */
    BlockPos getPosition();

    /**
     * Containers may be daisy-chained together.  If this container points to
     * another one, gets the root container in the chain.  If containers are
     * not daisy-chained, returns this container.  If there was a container
     * loop, returns null.
     *
     * @return the root pattern container
     */
    @Nullable
    ICraftingPatternContainer getRootContainer();

    /**
     * @return the UUID of this container
     */
    UUID getUuid();

    /**
     * @return true if the connected inventory is locked for processing patterns, false otherwise
     */
    default boolean isLocked() {
        return false;
    }

    /**
     * Unlock the container so it may be used by processing pattern
     */
    void unlock();

    /**
     * Called when this container is used by a processing pattern to insert items or fluids in the connected inventory.
     */
    default void onUsedForProcessing() {
    }

    /**
     * Called when the autocrafting system wants to insert items. Will be called with Action.SIMULATE first and if that
     * succeeds will be called again with Action.PERFORM
     *
     * @param toInsert A collection of items that should be inserted.
     * @param action   Action to take
     * @return whether the insertion was successful
     */
    boolean insertItemsIntoInventory(Collection<StackListEntry<ItemStack>> toInsert, Action action);

    /**
     * Called when the autocrafting system wants to insert fluids. Will be called with Action.SIMULATE first and if that
     * succeeds will be called again with Action.PERFORM
     *
     * @param toInsert A collection of fluids that should be inserted.
     * @param action   Action to take
     * @return whether the insertion was successful
     */
    boolean insertFluidsIntoInventory(Collection<StackListEntry<FluidStack>> toInsert, Action action);

    /**
     * @return whether the container is successfully connected to the inventory it wants to insert to
     */
    default boolean hasConnectedInventory() {
        return getConnectedInventory() != null;
    }

    /**
     * @return whether the container is successfully connected to the fluid inventory it wants to insert to
     */
    default boolean hasConnectedFluidInventory() {
        return getConnectedFluidInventory() != null;
    }
}
