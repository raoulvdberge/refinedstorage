package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.solderer.ISoldererRecipe;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeSolderer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileSolderer extends TileNode<NetworkNodeSolderer> {
    public static final TileDataParameter<Integer, TileSolderer> DURATION = new TileDataParameter<>(DataSerializers.VARINT, 0, t -> {
        ISoldererRecipe recipe = t.getNode().getRecipe();

        return recipe == null ? 0 : recipe.getDuration();
    });
    public static final TileDataParameter<Integer, TileSolderer> PROGRESS = new TileDataParameter<>(DataSerializers.VARINT, 0, t -> t.getNode().getProgress());
    public static final TileDataParameter<Boolean, TileSolderer> WORKING = new TileDataParameter<>(DataSerializers.BOOLEAN, false, t -> t.getNode().isWorking());

    private boolean working;

    public TileSolderer() {
        dataManager.addWatchedParameter(DURATION);
        dataManager.addWatchedParameter(PROGRESS);
        dataManager.addWatchedParameter(WORKING);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getNode().getItems());
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeUpdate(NBTTagCompound tag) {
        super.writeUpdate(tag);

        tag.setBoolean(NetworkNodeSolderer.NBT_WORKING, getNode().isWorking());

        return tag;
    }

    @Override
    public void readUpdate(NBTTagCompound tag) {
        super.readUpdate(tag);

        if (tag.hasKey(NetworkNodeSolderer.NBT_WORKING)) {
            working = tag.getBoolean(NetworkNodeSolderer.NBT_WORKING);
        }
    }

    public boolean isWorking() {
        return working;
    }

    @Override
    protected boolean canCauseRenderUpdate(NBTTagCompound tag) {
        EnumFacing receivedDirection = EnumFacing.getFront(tag.getInteger(NBT_DIRECTION));

        return receivedDirection != getDirection();
    }

    @Override
    @Nonnull
    public NetworkNodeSolderer createNode(World world, BlockPos pos) {
        return new NetworkNodeSolderer(world, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeSolderer.ID;
    }
}
