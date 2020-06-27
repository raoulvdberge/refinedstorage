package com.refinedmods.refinedstorage.tile;

import com.refinedmods.refinedstorage.RSTiles;
import com.refinedmods.refinedstorage.apiimpl.network.node.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.tile.config.IComparable;
import com.refinedmods.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InterfaceTile extends NetworkNodeTile<InterfaceNetworkNode> {
    public static final TileDataParameter<Integer, InterfaceTile> COMPARE = IComparable.createParameter();

    private final LazyOptional<IItemHandler> itemsCapability = LazyOptional.of(() -> getNode().getItems());

    public InterfaceTile() {
        super(RSTiles.INTERFACE);

        dataManager.addWatchedParameter(COMPARE);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemsCapability.cast();
        }

        return super.getCapability(cap, direction);
    }

    @Override
    @Nonnull
    public InterfaceNetworkNode createNode(World world, BlockPos pos) {
        return new InterfaceNetworkNode(world, pos);
    }
}
