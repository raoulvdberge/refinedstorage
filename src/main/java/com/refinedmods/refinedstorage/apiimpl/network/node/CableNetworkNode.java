package com.refinedmods.refinedstorage.apiimpl.network.node;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CableNetworkNode extends NetworkNode implements ICoverable {
    public static final ResourceLocation ID = new ResourceLocation(RS.ID, "cable");

    private CoverManager coverManager;

    public CableNetworkNode(World world, BlockPos pos) {
        super(world, pos);
        this.coverManager = new CoverManager(this);
    }

    @Override
    public int getEnergyUsage() {
        return RS.SERVER_CONFIG.getCable().getUsage();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CoverManager getCoverManager() {
        return coverManager;
    }

    @Override
    public void read(CompoundNBT tag) {
        if (tag.contains("Cover")) this.coverManager.readFromNbt(tag.getCompound("Cover"));
        super.read(tag);
    }

    @Override
    public void update() {
        super.update();
        //WorldUtils.updateBlock(world, pos);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("Cover", this.coverManager.writeToNbt());
        return super.write(tag);
    }
}
