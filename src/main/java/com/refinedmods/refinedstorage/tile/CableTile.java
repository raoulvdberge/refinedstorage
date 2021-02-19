package com.refinedmods.refinedstorage.tile;

import com.refinedmods.refinedstorage.RSTiles;
import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.apiimpl.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.node.DiskState;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.tile.data.TileDataManager;
import com.refinedmods.refinedstorage.tile.data.TileDataParameter;
import com.refinedmods.refinedstorage.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class CableTile extends NetworkNodeTile<CableNetworkNode> {

    public static final TileDataParameter<CompoundNBT, CableTile> COVER_MANAGER = new TileDataParameter<>(DataSerializers.COMPOUND_NBT, new CompoundNBT(), t -> t.getNode().getCoverManager().writeToNbt(), (t, v) -> t.getNode().getCoverManager().readFromNbt(v), (initial, p) -> Minecraft.getInstance().enqueue(() -> {

        }));

    static {
        TileDataManager.registerParameter(COVER_MANAGER);
    }

    public CableTile() {
        super(RSTiles.CABLE);
        dataManager.addWatchedParameter(COVER_MANAGER);
    }

    @Override
    @Nonnull
    public CableNetworkNode createNode(World world, BlockPos pos) {
        return new CableNetworkNode(world, pos);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(CoverManager.PROPERTY, this.getNode().getCoverManager()).build();
    }

    @Override
    public CompoundNBT writeUpdate(CompoundNBT tag) {
        super.writeUpdate(tag);
        tag.put("Covers", this.getNode().getCoverManager().writeToNbt());
        return tag;
    }

    @Override
    public void readUpdate(CompoundNBT tag) {
        super.readUpdate(tag);

        this.getNode().getCoverManager().readFromNbt(tag.getCompound("Covers"));

        requestModelDataUpdate();

        WorldUtils.updateBlock(world, pos);
    }


}
