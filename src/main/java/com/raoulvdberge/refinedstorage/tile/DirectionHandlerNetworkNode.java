package com.raoulvdberge.refinedstorage.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class DirectionHandlerNetworkNode implements IDirectionHandler {
    private TileNode tile;

    public DirectionHandlerNetworkNode(TileNode tile) {
        this.tile = tile;
    }

    @Override
    public void setDirection(EnumFacing direction) {
        tile.getNode().setDirection(direction);
    }

    @Override
    public EnumFacing getDirection() {
        return tile.getNode().getDirection();
    }

    @Override
    public void writeToTileNbt(NBTTagCompound tag) {
        // NO OP
    }

    @Override
    public void readFromTileNbt(NBTTagCompound tag) {
        // NO OP
    }
}
