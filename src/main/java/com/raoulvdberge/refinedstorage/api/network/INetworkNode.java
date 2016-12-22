package com.raoulvdberge.refinedstorage.api.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Represents a node in the network.
 */
public interface INetworkNode {
    /**
     * @return the energy usage of this node
     */
    int getEnergyUsage();

    /**
     * @return the position of this node in the world
     */
    BlockPos getPosition();

    /**
     * @return the item of the node
     */
    @Nonnull
    default ItemStack getItemStack() {
        IBlockState state = this.getNodeWorld().getBlockState(this.getPosition());
        return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
    }

    /**
     * Called when this node is connected to a network.
     *
     * @param network the network
     */
    void onConnected(INetworkMaster network);

    /**
     * Called when this node is disconnected from a network.
     *
     * @param network the network
     */
    void onDisconnected(INetworkMaster network);

    /**
     * @return true if this is node is connected to a network, or false otherwise
     */
    default boolean isConnected() {
        return getNetwork() != null;
    }

    /**
     * @return true if this node can be treated as active, typically checks the redstone configuration
     */
    boolean canUpdate();

    /**
     * @param direction the direction to do a conduction check
     * @return true if this node can conduct a connection to the given direction, false otherwise
     */
    boolean canConduct(EnumFacing direction);

    /**
     * @return the network
     */
    INetworkMaster getNetwork();

    /**
     * @return the world where this node is in
     */
    World getNodeWorld();
}
