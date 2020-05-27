package com.refinedmods.refinedstorage.container;

import com.refinedmods.refinedstorage.RSContainers;
import com.refinedmods.refinedstorage.tile.WirelessTransmitterTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.SlotItemHandler;

public class WirelessTransmitterContainer extends BaseContainer {
    public WirelessTransmitterContainer(WirelessTransmitterTile wirelessTransmitter, PlayerEntity player, int windowId) {
        super(RSContainers.WIRELESS_TRANSMITTER, wirelessTransmitter, player, windowId);

        for (int i = 0; i < 4; ++i) {
            addSlot(new SlotItemHandler(wirelessTransmitter.getNode().getUpgrades(), i, 187, 6 + (i * 18)));
        }

        addPlayerInventory(8, 55);

        transferManager.addBiTransfer(player.inventory, wirelessTransmitter.getNode().getUpgrades());
    }
}
