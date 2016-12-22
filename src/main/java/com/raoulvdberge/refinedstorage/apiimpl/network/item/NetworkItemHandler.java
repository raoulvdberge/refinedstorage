package com.raoulvdberge.refinedstorage.apiimpl.network.item;

import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.IWirelessTransmitter;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItem;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItemHandler;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItemProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworkItemHandler implements INetworkItemHandler {
    private INetworkMaster network;

    private List<INetworkItem> items = new ArrayList<>();
    private List<INetworkItem> itemsToRemove = new ArrayList<>();

    public NetworkItemHandler(INetworkMaster network) {
        this.network = network;
    }

    @Override
    public void update() {
        items.removeAll(itemsToRemove);
        itemsToRemove.clear();
    }

    @Override
    public boolean onOpen(EntityPlayer player, World controllerWorld, EnumHand hand) {
        boolean inRange = false;

        for (INetworkNode node : network.getNodeGraph().all()) {
            if (node instanceof IWirelessTransmitter && ((IWirelessTransmitter) node).getDimension() == player.dimension) {
                IWirelessTransmitter transmitter = (IWirelessTransmitter) node;

                double distance = Math.sqrt(Math.pow(transmitter.getOrigin().getX() - player.posX, 2) + Math.pow(transmitter.getOrigin().getY() - player.posY, 2) + Math.pow(transmitter.getOrigin().getZ() - player.posZ, 2));

                if (distance < transmitter.getRange()) {
                    inRange = true;

                    break;
                }
            }
        }

        if (!inRange) {
            return false;
        }

        ItemStack stack = player.getHeldItem(hand);

        INetworkItem item = ((INetworkItemProvider) stack.getItem()).provide(this, player, stack);

        if (item.onOpen(network, player, controllerWorld, hand)) {
            items.add(item);

            return true;
        }

        return false;
    }

    @Override
    public void onClose(EntityPlayer player) {
        INetworkItem item = getItem(player);

        if (item != null) {
            itemsToRemove.add(item);
        }
    }

    @Override
    public INetworkItem getItem(EntityPlayer player) {
        Iterator<INetworkItem> it = items.iterator();

        while (it.hasNext()) {
            INetworkItem item = it.next();

            if (item.getPlayer() == player) {
                return item;
            }
        }

        return null;
    }
}
