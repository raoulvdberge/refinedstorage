package com.refinedmods.refinedstorage.apiimpl.storage.cache.listener;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCacheListener;
import com.refinedmods.refinedstorage.api.util.StackListResult;
import com.refinedmods.refinedstorage.network.grid.GridFluidDeltaMessage;
import com.refinedmods.refinedstorage.network.grid.GridFluidUpdateMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidGridStorageCacheListener implements IStorageCacheListener<FluidStack> {
    private ServerPlayerEntity player;
    private INetwork network;

    public FluidGridStorageCacheListener(ServerPlayerEntity player, INetwork network) {
        this.player = player;
        this.network = network;
    }

    @Override
    public void onAttached() {
        RS.NETWORK_HANDLER.sendTo(player, new GridFluidUpdateMessage(network, network.getSecurityManager().hasPermission(Permission.AUTOCRAFTING, player)));
    }

    @Override
    public void onInvalidated() {
        // NO OP
    }

    @Override
    public void onChanged(StackListResult<FluidStack> delta) {
        List<StackListResult<FluidStack>> deltas = new ArrayList<>();

        deltas.add(delta);

        onChangedBulk(deltas);
    }

    @Override
    public void onChangedBulk(List<StackListResult<FluidStack>> deltas) {
        RS.NETWORK_HANDLER.sendTo(player, new GridFluidDeltaMessage(network, deltas));
    }
}
