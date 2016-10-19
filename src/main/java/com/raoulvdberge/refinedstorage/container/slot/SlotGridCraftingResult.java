package com.raoulvdberge.refinedstorage.container.slot;

import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.tile.grid.TileGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SlotGridCraftingResult extends SlotCrafting {
    private ContainerGrid container;
    private TileGrid grid;

    public SlotGridCraftingResult(ContainerGrid container, EntityPlayer player, TileGrid grid, int id, int x, int y) {
        super(player, grid.getMatrix(), grid.getResult(), id, x, y);

        this.container = container;
        this.grid = grid;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, grid.getMatrix());

        onCrafting(stack);

        if (!player.worldObj.isRemote) {
            grid.onCrafted(player);

            container.sendCraftingSlots();
        }
    }
}
