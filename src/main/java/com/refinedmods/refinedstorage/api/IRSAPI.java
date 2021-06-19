package com.refinedmods.refinedstorage.api;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternRenderHandler;
import com.refinedmods.refinedstorage.api.autocrafting.craftingmonitor.ICraftingMonitorElementList;
import com.refinedmods.refinedstorage.api.autocrafting.craftingmonitor.ICraftingMonitorElementRegistry;
import com.refinedmods.refinedstorage.api.autocrafting.preview.ICraftingPreviewElementRegistry;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICraftingRequestInfo;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICraftingTaskRegistry;
import com.refinedmods.refinedstorage.api.network.INetworkManager;
import com.refinedmods.refinedstorage.api.network.grid.ICraftingGridBehavior;
import com.refinedmods.refinedstorage.api.network.grid.IGridManager;
import com.refinedmods.refinedstorage.api.network.node.INetworkNodeManager;
import com.refinedmods.refinedstorage.api.network.node.INetworkNodeRegistry;
import com.refinedmods.refinedstorage.api.storage.StorageType;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDiskManager;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDiskRegistry;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDiskSync;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorageProvider;
import com.refinedmods.refinedstorage.api.storage.tracker.IStorageTrackerManager;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.api.util.IQuantityFormatter;
import com.refinedmods.refinedstorage.api.util.IStackList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Represents a Refined Storage API implementation.
 * Delivered by the {@link RSAPIInject} annotation.
 */
public interface IRSAPI {
    /**
     * @return the comparer
     */
    @Nonnull
    IComparer getComparer();

    /**
     * @return the quantity formatter
     */
    @Nonnull
    IQuantityFormatter getQuantityFormatter();

    /**
     * @return the network node factory
     */
    @Nonnull
    INetworkNodeRegistry getNetworkNodeRegistry();

    /**
     * Gets a network node manager for a given world.
     *
     * @param world world
     * @return the network node manager for a given world
     */
    INetworkNodeManager getNetworkNodeManager(ServerWorld world);

    /**
     * Gets a network manager for a given world.
     *
     * @param world world
     * @return the network manager for a given world
     */
    INetworkManager getNetworkManager(ServerWorld world);

    /**
     * @return the crafting task registry
     */
    @Nonnull
    ICraftingTaskRegistry getCraftingTaskRegistry();

    /**
     * @return the crafting monitor element registry
     */
    @Nonnull
    ICraftingMonitorElementRegistry getCraftingMonitorElementRegistry();

    /**
     * @return the crafting preview element registry
     */
    @Nonnull
    ICraftingPreviewElementRegistry getCraftingPreviewElementRegistry();

    /**
     * @return an empty item stack list
     */
    @Nonnull
    IStackList<ItemStack> createItemStackList();

    /**
     * @return an empty fluid stack list
     */
    @Nonnull
    IStackList<FluidStack> createFluidStackList();

    /**
     * @return an empty crafting monitor element list
     */
    @Nonnull
    ICraftingMonitorElementList createCraftingMonitorElementList();

    /**
     * @return the grid manager
     */
    @Nonnull
    IGridManager getGridManager();

    /**
     * @return the default crafting grid behavior
     */
    @Nonnull
    ICraftingGridBehavior getCraftingGridBehavior();

    /**
     * @return the storage disk registry
     */
    @Nonnull
    IStorageDiskRegistry getStorageDiskRegistry();

    /**
     * @param anyWorld any world associated with the server
     * @return the storage disk manager
     */
    @Nonnull
    IStorageDiskManager getStorageDiskManager(ServerWorld anyWorld);

    /**
     * @return the storage disk sync manager
     */
    @Nonnull
    IStorageDiskSync getStorageDiskSync();

    /**
     * @return the storage tracker manager
     */
    @Nonnull
    IStorageTrackerManager getStorageTrackerManager(ServerWorld anyWorld);

    /**
     * Adds an external storage provider for the given storage type.
     *
     * @param type     the storage type
     * @param provider the external storage provider
     */
    void addExternalStorageProvider(StorageType type, IExternalStorageProvider<?> provider);

    /**
     * @param type the type
     * @return a set of external storage providers
     */
    <T> Set<IExternalStorageProvider<T>> getExternalStorageProviders(StorageType type);

    /**
     * @param world    the world
     * @param capacity the capacity
     * @param owner    the owner or null if no owner
     * @return a storage disk
     */
    @Nonnull
    IStorageDisk<ItemStack> createDefaultItemDisk(ServerWorld world, int capacity, @Nullable PlayerEntity owner);

    /**
     * @param world    the world
     * @param capacity the capacity in mB
     * @param owner    the owner or null if no owner
     * @return a fluid storage disk
     */
    @Nonnull
    IStorageDisk<FluidStack> createDefaultFluidDisk(ServerWorld world, int capacity, @Nullable PlayerEntity owner);

    /**
     * Creates crafting request info for an item.
     *
     * @param stack the stack
     * @param count the count
     * @return the request info
     */
    ICraftingRequestInfo createCraftingRequestInfo(ItemStack stack, int count);

    /**
     * Creates crafting request info for a fluid.
     *
     * @param stack the stack
     * @param count the count
     * @return the request info
     */
    ICraftingRequestInfo createCraftingRequestInfo(FluidStack stack, int count);

    /**
     * Creates crafting request info from NBT.
     *
     * @param tag the nbt tag
     * @return the request info
     */
    ICraftingRequestInfo createCraftingRequestInfo(CompoundNBT tag) throws CraftingTaskReadException;

    /**
     * @param renderHandler the render handler to add
     */
    void addPatternRenderHandler(ICraftingPatternRenderHandler renderHandler);

    /**
     * @return a list of pattern render handlers
     */
    List<ICraftingPatternRenderHandler> getPatternRenderHandlers();

    /**
     * @param stack the stack
     * @return a hashcode for the given stack
     */
    int getItemStackHashCode(ItemStack stack);

    /**
     * @param stack the stack
     * @return a hashcode for the given stack
     */
    int getFluidStackHashCode(FluidStack stack);
}
