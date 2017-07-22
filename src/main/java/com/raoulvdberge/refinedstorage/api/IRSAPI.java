package com.raoulvdberge.refinedstorage.api;

import com.raoulvdberge.refinedstorage.api.autocrafting.craftingmonitor.ICraftingMonitorElementList;
import com.raoulvdberge.refinedstorage.api.autocrafting.craftingmonitor.ICraftingMonitorElementRegistry;
import com.raoulvdberge.refinedstorage.api.autocrafting.preview.ICraftingPreviewElementRegistry;
import com.raoulvdberge.refinedstorage.api.autocrafting.registry.ICraftingTaskRegistry;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.grid.wireless.IWirelessGridFactory;
import com.raoulvdberge.refinedstorage.api.network.grid.wireless.IWirelessGridRegistry;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeRegistry;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandlerRegistry;
import com.raoulvdberge.refinedstorage.api.solderer.ISoldererRegistry;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskBehavior;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.api.util.IStackList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

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
     * @return the network node factory
     */
    INetworkNodeRegistry getNetworkNodeRegistry();

    /**
     * Gets a network node manager for a given world.
     * This can only be called on the server side!
     * There is no such concept of a network node manager on the client.
     *
     * @param world the world
     * @return the network node manager for the given world
     */
    INetworkNodeManager getNetworkNodeManager(World world);

    /**
     * @return the default storage disk behavior
     */
    IStorageDiskBehavior getDefaultStorageDiskBehavior();

    /**
     * @return the solderer registry
     */
    @Nonnull
    ISoldererRegistry getSoldererRegistry();

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
     * @return the reader writer handler registry
     */
    @Nonnull
    IReaderWriterHandlerRegistry getReaderWriterHandlerRegistry();

    /**
     * @return a new reader writer channel
     */
    @Nonnull
    IReaderWriterChannel createReaderWriterChannel(String name, INetwork network);

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
     * @return the wireless grid registry
     */
    @Nonnull
    IWirelessGridRegistry getWirelessGridRegistry();

    /**
     * Opens a wireless grid for the given player.
     *
     * @param player           the player
     * @param hand             the hand where the wireless grid is in
     * @param networkDimension the dimension of the bound network
     * @param id               the id of the wireless grid, as returned in {@link IWirelessGridRegistry#add(IWirelessGridFactory)}
     */
    void openWirelessGrid(EntityPlayer player, EnumHand hand, int networkDimension, int id);

    /**
     * Notifies the neighbors of a node that there is a node placed at the given position.
     *
     * @param world the world
     * @param pos   the position of the node
     */
    void discoverNode(World world, BlockPos pos);

    /**
     * @param stack the stack
     * @param tag   whether the NBT tag of the stack should be calculated in the hashcode, used for performance reasons
     * @return a hashcode for the given stack
     */
    int getItemStackHashCode(ItemStack stack, boolean tag);

    /**
     * @param stack the stack
     * @return a hashcode for the given stack
     */
    default int getItemStackHashCode(ItemStack stack) {
        return getItemStackHashCode(stack, true);
    }

    /**
     * @param stack the stack
     * @return a hashcode for the given stack
     */
    int getFluidStackHashCode(FluidStack stack);

    /**
     * @param node the node
     * @return the hashcode
     */
    int getNetworkNodeHashCode(INetworkNode node);

    /**
     * @param left  the first network node
     * @param right the second network node
     * @return true if the two network nodes are equal, false otherwise
     */
    boolean isNetworkNodeEqual(INetworkNode left, Object right);
}
