package com.refinedmods.refinedstorage.network;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.network.craftingmonitor.CraftingMonitorCancelMessage;
import com.refinedmods.refinedstorage.network.craftingmonitor.CraftingMonitorUpdateMessage;
import com.refinedmods.refinedstorage.network.craftingmonitor.WirelessCraftingMonitorSettingsUpdateMessage;
import com.refinedmods.refinedstorage.network.disk.StorageDiskSizeRequestMessage;
import com.refinedmods.refinedstorage.network.disk.StorageDiskSizeResponseMessage;
import com.refinedmods.refinedstorage.network.grid.*;
import com.refinedmods.refinedstorage.network.tiledata.TileDataParameterMessage;
import com.refinedmods.refinedstorage.network.tiledata.TileDataParameterUpdateMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private final String protocolVersion = Integer.toString(1);
    private final SimpleChannel handler = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(RS.ID, "main_channel"))
        .clientAcceptedVersions(protocolVersion::equals)
        .serverAcceptedVersions(protocolVersion::equals)
        .networkProtocolVersion(() -> protocolVersion)
        .simpleChannel();

    public void register() {
        int id = 0;

        handler.registerMessage(id++, StorageDiskSizeRequestMessage.class, StorageDiskSizeRequestMessage::encode, StorageDiskSizeRequestMessage::decode, StorageDiskSizeRequestMessage::handle);
        handler.registerMessage(id++, StorageDiskSizeResponseMessage.class, StorageDiskSizeResponseMessage::encode, StorageDiskSizeResponseMessage::decode, StorageDiskSizeResponseMessage::handle);
        handler.registerMessage(id++, FilterUpdateMessage.class, FilterUpdateMessage::encode, FilterUpdateMessage::decode, FilterUpdateMessage::handle);
        handler.registerMessage(id++, FluidFilterSlotUpdateMessage.class, FluidFilterSlotUpdateMessage::encode, FluidFilterSlotUpdateMessage::decode, FluidFilterSlotUpdateMessage::handle);
        handler.registerMessage(id++, TileDataParameterMessage.class, TileDataParameterMessage::encode, TileDataParameterMessage::decode, TileDataParameterMessage::handle);
        handler.registerMessage(id++, TileDataParameterUpdateMessage.class, TileDataParameterUpdateMessage::encode, TileDataParameterUpdateMessage::decode, TileDataParameterUpdateMessage::handle);
        handler.registerMessage(id++, GridItemUpdateMessage.class, GridItemUpdateMessage::encode, GridItemUpdateMessage::decode, GridItemUpdateMessage::handle);
        handler.registerMessage(id++, GridItemDeltaMessage.class, GridItemDeltaMessage::encode, GridItemDeltaMessage::decode, GridItemDeltaMessage::handle);
        handler.registerMessage(id++, GridItemPullMessage.class, GridItemPullMessage::encode, GridItemPullMessage::decode, GridItemPullMessage::handle);
        handler.registerMessage(id++, GridItemGridScrollMessage.class, GridItemGridScrollMessage::encode, GridItemGridScrollMessage::decode, GridItemGridScrollMessage::handle);
        handler.registerMessage(id++, GridItemInventoryScrollMessage.class, GridItemInventoryScrollMessage::encode, GridItemInventoryScrollMessage::decode, GridItemInventoryScrollMessage::handle);
        handler.registerMessage(id++, GridItemInsertHeldMessage.class, GridItemInsertHeldMessage::encode, GridItemInsertHeldMessage::decode, GridItemInsertHeldMessage::handle);
        handler.registerMessage(id++, GridClearMessage.class, GridClearMessage::encode, GridClearMessage::decode, GridClearMessage::handle);
        handler.registerMessage(id++, GridPatternCreateMessage.class, GridPatternCreateMessage::encode, GridPatternCreateMessage::decode, GridPatternCreateMessage::handle);
        handler.registerMessage(id++, SetFilterSlotMessage.class, SetFilterSlotMessage::encode, SetFilterSlotMessage::decode, SetFilterSlotMessage::handle);
        handler.registerMessage(id++, SetFluidFilterSlotMessage.class, SetFluidFilterSlotMessage::encode, SetFluidFilterSlotMessage::decode, SetFluidFilterSlotMessage::handle);
        handler.registerMessage(id++, GridFluidUpdateMessage.class, GridFluidUpdateMessage::encode, GridFluidUpdateMessage::decode, GridFluidUpdateMessage::handle);
        handler.registerMessage(id++, GridFluidDeltaMessage.class, GridFluidDeltaMessage::encode, GridFluidDeltaMessage::decode, GridFluidDeltaMessage::handle);
        handler.registerMessage(id++, GridFluidInsertHeldMessage.class, GridFluidInsertHeldMessage::encode, GridFluidInsertHeldMessage::decode, GridFluidInsertHeldMessage::handle);
        handler.registerMessage(id++, GridFluidPullMessage.class, GridFluidPullMessage::encode, GridFluidPullMessage::decode, GridFluidPullMessage::handle);
        handler.registerMessage(id++, GridTransferMessage.class, GridTransferMessage::encode, GridTransferMessage::decode, GridTransferMessage::handle);
        handler.registerMessage(id++, GridProcessingTransferMessage.class, GridProcessingTransferMessage::encode, GridProcessingTransferMessage::decode, GridProcessingTransferMessage::handle);
        handler.registerMessage(id++, SecurityManagerUpdateMessage.class, SecurityManagerUpdateMessage::encode, SecurityManagerUpdateMessage::decode, SecurityManagerUpdateMessage::handle);
        handler.registerMessage(id++, WirelessGridSettingsUpdateMessage.class, WirelessGridSettingsUpdateMessage::encode, WirelessGridSettingsUpdateMessage::decode, WirelessGridSettingsUpdateMessage::handle);
        handler.registerMessage(id++, OpenNetworkItemMessage.class, OpenNetworkItemMessage::encode, OpenNetworkItemMessage::decode, OpenNetworkItemMessage::handle);
        handler.registerMessage(id++, WirelessFluidGridSettingsUpdateMessage.class, WirelessFluidGridSettingsUpdateMessage::encode, WirelessFluidGridSettingsUpdateMessage::decode, WirelessFluidGridSettingsUpdateMessage::handle);
        handler.registerMessage(id++, PortableGridSettingsUpdateMessage.class, PortableGridSettingsUpdateMessage::encode, PortableGridSettingsUpdateMessage::decode, PortableGridSettingsUpdateMessage::handle);
        handler.registerMessage(id++, PortableGridItemUpdateMessage.class, PortableGridItemUpdateMessage::encode, PortableGridItemUpdateMessage::decode, PortableGridItemUpdateMessage::handle);
        handler.registerMessage(id++, PortableGridItemDeltaMessage.class, PortableGridItemDeltaMessage::encode, PortableGridItemDeltaMessage::decode, PortableGridItemDeltaMessage::handle);
        handler.registerMessage(id++, PortableGridFluidUpdateMessage.class, PortableGridFluidUpdateMessage::encode, PortableGridFluidUpdateMessage::decode, PortableGridFluidUpdateMessage::handle);
        handler.registerMessage(id++, PortableGridFluidDeltaMessage.class, PortableGridFluidDeltaMessage::encode, PortableGridFluidDeltaMessage::decode, PortableGridFluidDeltaMessage::handle);
        handler.registerMessage(id++, GridCraftingPreviewRequestMessage.class, GridCraftingPreviewRequestMessage::encode, GridCraftingPreviewRequestMessage::decode, GridCraftingPreviewRequestMessage::handle);
        handler.registerMessage(id++, GridCraftingPreviewResponseMessage.class, GridCraftingPreviewResponseMessage::encode, GridCraftingPreviewResponseMessage::decode, GridCraftingPreviewResponseMessage::handle);
        handler.registerMessage(id++, GridCraftingStartRequestMessage.class, GridCraftingStartRequestMessage::encode, GridCraftingStartRequestMessage::decode, GridCraftingStartRequestMessage::handle);
        handler.registerMessage(id++, GridCraftingStartResponseMessage.class, GridCraftingStartResponseMessage::encode, GridCraftingStartResponseMessage::decode, GridCraftingStartResponseMessage::handle);
        handler.registerMessage(id++, CraftingMonitorUpdateMessage.class, CraftingMonitorUpdateMessage::encode, CraftingMonitorUpdateMessage::decode, CraftingMonitorUpdateMessage::handle);
        handler.registerMessage(id++, CraftingMonitorCancelMessage.class, CraftingMonitorCancelMessage::encode, CraftingMonitorCancelMessage::decode, CraftingMonitorCancelMessage::handle);
        handler.registerMessage(id++, WirelessCraftingMonitorSettingsUpdateMessage.class, WirelessCraftingMonitorSettingsUpdateMessage::encode, WirelessCraftingMonitorSettingsUpdateMessage::decode, WirelessCraftingMonitorSettingsUpdateMessage::handle);
    }

    public void sendToServer(Object message) {
        handler.sendToServer(message);
    }

    public void sendTo(ServerPlayerEntity player, Object message) {
        if (!(player instanceof FakePlayer)) {
            handler.sendTo(message, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
