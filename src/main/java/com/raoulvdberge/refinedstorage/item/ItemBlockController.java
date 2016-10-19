package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.block.EnumControllerType;
import com.raoulvdberge.refinedstorage.tile.TileController;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemBlockController extends ItemBlockBase {
    public ItemBlockController() {
        super(RSBlocks.CONTROLLER, RSBlocks.CONTROLLER.getPlacementType(), true);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (stack.getMetadata() != EnumControllerType.CREATIVE.getId()) {
            tooltip.add(I18n.format("misc.refinedstorage:energy_stored", getEnergyStored(stack), getEnergyCapacity(stack)));
        }
    }

    public static int getEnergyStored(ItemStack stack) {
        return (stack.hasTagCompound() && stack.getTagCompound().hasKey(TileController.NBT_ENERGY)) ? stack.getTagCompound().getInteger(TileController.NBT_ENERGY) : 0;
    }

    public static int getEnergyCapacity(ItemStack stack) {
        return (stack.hasTagCompound() && stack.getTagCompound().hasKey(TileController.NBT_ENERGY_CAPACITY)) ? stack.getTagCompound().getInteger(TileController.NBT_ENERGY_CAPACITY) : RS.INSTANCE.config.controllerCapacity;
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);

        createStackWithNBT(stack);
    }

    public static ItemStack createStackWithNBT(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
        }

        tag.setInteger(TileController.NBT_ENERGY, stack.getMetadata() == EnumControllerType.CREATIVE.getId() ? RS.INSTANCE.config.controllerCapacity : 0);

        return stack;
    }
}
