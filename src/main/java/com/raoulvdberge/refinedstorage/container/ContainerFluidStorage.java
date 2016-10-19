package com.raoulvdberge.refinedstorage.container;

import com.raoulvdberge.refinedstorage.container.slot.SlotSpecimenFluid;
import com.raoulvdberge.refinedstorage.tile.TileFluidStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFluidStorage extends ContainerBase {
    public ContainerFluidStorage(TileFluidStorage tile, EntityPlayer player) {
        super(tile, player);

        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new SlotSpecimenFluid(!tile.getWorld().isRemote, tile.getFilters(), i, 8 + (18 * i), 20));
        }

        addPlayerInventory(8, 129);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = getSlot(index);

        if (slot != null && slot.getHasStack() && index >= 8) {
            return mergeItemStackToSpecimen(slot.getStack(), 0, 9);
        }

        return null;
    }
}
