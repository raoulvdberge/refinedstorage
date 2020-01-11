package com.raoulvdberge.refinedstorage.apiimpl.network.grid;

import com.raoulvdberge.refinedstorage.api.network.grid.GridFactoryType;
import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.api.network.grid.IGridFactory;
import com.raoulvdberge.refinedstorage.tile.grid.portable.PortableGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GridFactoryPortableGrid implements IGridFactory {
    @Nullable
    @Override
    public IGrid createFromStack(EntityPlayer player, ItemStack stack, int slotId) {
        return new PortableGrid(player, stack, slotId);
    }

    @Nullable
    @Override
    public IGrid createFromBlock(EntityPlayer player, BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public TileEntity getRelevantTile(World world, BlockPos pos) {
        return null;
    }

    @Override
    public GridFactoryType getType() {
        return GridFactoryType.STACK;
    }
}
