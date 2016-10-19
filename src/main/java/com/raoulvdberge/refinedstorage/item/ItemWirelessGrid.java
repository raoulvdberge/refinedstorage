package com.raoulvdberge.refinedstorage.item;

import cofh.api.energy.ItemEnergyContainer;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.integration.forgeenergy.WirelessGridEnergyForge;
import com.raoulvdberge.refinedstorage.integration.ic2.IntegrationIC2;
import com.raoulvdberge.refinedstorage.integration.tesla.IntegrationTesla;
import com.raoulvdberge.refinedstorage.integration.tesla.WirelessGridEnergyTesla;
import com.raoulvdberge.refinedstorage.tile.TileController;
import com.raoulvdberge.refinedstorage.tile.grid.TileGrid;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.List;

@Optional.InterfaceList({
    @Optional.Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = "IC2"),
    @Optional.Interface(iface = "ic2.api.item.IElectricItemManager", modid = "IC2")
})
public class ItemWirelessGrid extends ItemEnergyContainer implements ISpecialElectricItem, IElectricItemManager {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_CREATIVE = 1;

    private static final String NBT_CONTROLLER_X = "ControllerX";
    private static final String NBT_CONTROLLER_Y = "ControllerY";
    private static final String NBT_CONTROLLER_Z = "ControllerZ";
    private static final String NBT_DIMENSION_ID = "DimensionID";

    public ItemWirelessGrid() {
        super(3200);

        setRegistryName(RS.ID, "wireless_grid");
        setMaxDamage(3200);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setCreativeTab(RS.INSTANCE.tab);
        addPropertyOverride(new ResourceLocation("connected"), (stack, world, entity) -> (entity != null && isValid(stack)) ? 1.0f : 0.0f);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new WirelessGridCapabilityProvider(stack);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((double) getEnergyStored(stack) / (double) getMaxEnergyStored(stack));
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return stack.getItemDamage() != TYPE_CREATIVE;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        // NO OP
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, TYPE_NORMAL));

        ItemStack fullyCharged = new ItemStack(item, 1, TYPE_NORMAL);
        receiveEnergy(fullyCharged, getMaxEnergyStored(fullyCharged), false);
        list.add(fullyCharged);

        list.add(new ItemStack(item, 1, TYPE_CREATIVE));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        if (stack.getItemDamage() != TYPE_CREATIVE) {
            tooltip.add(I18n.format("misc.refinedstorage:energy_stored", getEnergyStored(stack), getMaxEnergyStored(stack)));
        }

        if (isValid(stack)) {
            tooltip.add(I18n.format("misc.refinedstorage:wireless_grid.tooltip", getX(stack), getY(stack), getZ(stack)));
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == RSBlocks.CONTROLLER) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag == null) {
                tag = new NBTTagCompound();
            }

            tag.setInteger(NBT_CONTROLLER_X, pos.getX());
            tag.setInteger(NBT_CONTROLLER_Y, pos.getY());
            tag.setInteger(NBT_CONTROLLER_Z, pos.getZ());
            tag.setInteger(NBT_DIMENSION_ID, player.dimension);
            tag.setInteger(TileGrid.NBT_VIEW_TYPE, TileGrid.VIEW_TYPE_NORMAL);
            tag.setInteger(TileGrid.NBT_SORTING_DIRECTION, TileGrid.SORTING_DIRECTION_DESCENDING);
            tag.setInteger(TileGrid.NBT_SORTING_TYPE, TileGrid.SORTING_TYPE_NAME);
            tag.setInteger(TileGrid.NBT_SEARCH_BOX_MODE, TileGrid.SEARCH_BOX_MODE_NORMAL);

            stack.setTagCompound(tag);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && isValid(stack)) {
            World controllerWorld = DimensionManager.getWorld(getDimensionId(stack));

            TileEntity controller;

            if (controllerWorld != null && ((controller = controllerWorld.getTileEntity(new BlockPos(getX(stack), getY(stack), getZ(stack)))) instanceof TileController)) {
                if (((TileController) controller).getWirelessGridHandler().onOpen(player, controllerWorld, hand)) {
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                } else {
                    player.addChatComponentMessage(new TextComponentTranslation("misc.refinedstorage:wireless_grid.out_of_range"));
                }
            } else {
                player.addChatComponentMessage(new TextComponentTranslation("misc.refinedstorage:wireless_grid.not_found"));
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public static int getDimensionId(ItemStack stack) {
        return stack.getTagCompound().getInteger(NBT_DIMENSION_ID);
    }

    public static int getX(ItemStack stack) {
        return stack.getTagCompound().getInteger(NBT_CONTROLLER_X);
    }

    public static int getY(ItemStack stack) {
        return stack.getTagCompound().getInteger(NBT_CONTROLLER_Y);
    }

    public static int getZ(ItemStack stack) {
        return stack.getTagCompound().getInteger(NBT_CONTROLLER_Z);
    }

    public static int getViewType(ItemStack stack) {
        return stack.getTagCompound().getInteger(TileGrid.NBT_VIEW_TYPE);
    }

    public static int getSortingType(ItemStack stack) {
        return stack.getTagCompound().getInteger(TileGrid.NBT_SORTING_TYPE);
    }

    public static int getSortingDirection(ItemStack stack) {
        return stack.getTagCompound().getInteger(TileGrid.NBT_SORTING_DIRECTION);
    }

    public static int getSearchBoxMode(ItemStack stack) {
        return stack.getTagCompound().getInteger(TileGrid.NBT_SEARCH_BOX_MODE);
    }

    private static boolean isValid(ItemStack stack) {
        return stack.hasTagCompound()
            && stack.getTagCompound().hasKey(NBT_CONTROLLER_X)
            && stack.getTagCompound().hasKey(NBT_CONTROLLER_Y)
            && stack.getTagCompound().hasKey(NBT_CONTROLLER_Z)
            && stack.getTagCompound().hasKey(NBT_DIMENSION_ID)
            && stack.getTagCompound().hasKey(TileGrid.NBT_VIEW_TYPE)
            && stack.getTagCompound().hasKey(TileGrid.NBT_SORTING_DIRECTION)
            && stack.getTagCompound().hasKey(TileGrid.NBT_SORTING_TYPE)
            && stack.getTagCompound().hasKey(TileGrid.NBT_SEARCH_BOX_MODE);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == newStack.getItem()) {
            if (isValid(oldStack) && isValid(newStack)) {
                if (getX(oldStack) == getX(newStack) && getY(oldStack) == getY(newStack) && getZ(oldStack) == getZ(newStack) && getDimensionId(oldStack) == getDimensionId(newStack)) {
                    return false;
                }
            }
        }

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public String getUnlocalizedName() {
        return "item." + RS.ID + ":wireless_grid";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName() + "." + stack.getItemDamage();
    }

    @Optional.Method(modid = "IC2")
    @Override
    public IElectricItemManager getManager(ItemStack stack) {
        return this;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        return IntegrationIC2.toEU(receiveEnergy(stack, IntegrationIC2.toRS(amount), simulate));
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        return IntegrationIC2.toEU(extractEnergy(stack, IntegrationIC2.toRS(amount), simulate));
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double getCharge(ItemStack stack) {
        return IntegrationIC2.toEU(getEnergyStored(stack));
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double getMaxCharge(ItemStack stack) {
        return IntegrationIC2.toEU(getMaxEnergyStored(stack));
    }

    @Optional.Method(modid = "IC2")
    @Override
    public boolean canUse(ItemStack stack, double amount) {
        return true;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
        return true;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {
        // NO OP
    }

    @Optional.Method(modid = "IC2")
    @Override
    public String getToolTip(ItemStack stack) {
        return null;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public int getTier(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    class WirelessGridCapabilityProvider implements ICapabilityProvider {
        private ItemStack stack;

        public WirelessGridCapabilityProvider(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY ||
                (IntegrationTesla.isLoaded() && (capability == TeslaCapabilities.CAPABILITY_HOLDER || capability == TeslaCapabilities.CAPABILITY_CONSUMER));
        }

        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return (T) new WirelessGridEnergyForge(stack);
            }

            if (IntegrationTesla.isLoaded() && (capability == TeslaCapabilities.CAPABILITY_HOLDER || capability == TeslaCapabilities.CAPABILITY_CONSUMER)) {
                return (T) new WirelessGridEnergyTesla(stack);
            }

            return null;
        }
    }
}
