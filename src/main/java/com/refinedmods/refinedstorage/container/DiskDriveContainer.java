package com.refinedmods.refinedstorage.container;

import com.refinedmods.refinedstorage.RSContainers;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.tile.DiskDriveTile;
import com.refinedmods.refinedstorage.tile.config.IType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.SlotItemHandler;

public class DiskDriveContainer extends BaseContainer {
    public DiskDriveContainer(DiskDriveTile diskDrive, PlayerEntity player, int windowId) {
        super(RSContainers.DISK_DRIVE, diskDrive, player, windowId);

        int x = 80;
        int y = 54;

        for (int i = 0; i < 8; ++i) {
            addSlot(new SlotItemHandler(diskDrive.getNode().getDisks(), i, x + ((i % 2) * 18), y + Math.floorDiv(i, 2) * 18));
        }

        for (int i = 0; i < 9; ++i) {
            addSlot(new FilterSlot(diskDrive.getNode().getItemFilters(), i, 8 + (18 * i), 20).setEnableHandler(() -> diskDrive.getNode().getType() == IType.ITEMS));
        }

        for (int i = 0; i < 9; ++i) {
            addSlot(new FluidFilterSlot(diskDrive.getNode().getFluidFilters(), i, 8 + (18 * i), 20).setEnableHandler(() -> diskDrive.getNode().getType() == IType.FLUIDS));
        }

        addPlayerInventory(8, 141);

        transferManager.addBiTransfer(player.inventory, diskDrive.getNode().getDisks());
        transferManager.addFilterTransfer(player.inventory, diskDrive.getNode().getItemFilters(), diskDrive.getNode().getFluidFilters(), diskDrive.getNode()::getType);
    }
}
