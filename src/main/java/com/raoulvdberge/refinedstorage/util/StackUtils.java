package com.raoulvdberge.refinedstorage.util;

import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskProvider;
import com.raoulvdberge.refinedstorage.api.util.IStackList;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class StackUtils {
    public static final ItemStack EMPTY_BUCKET = new ItemStack(Items.BUCKET);
    public static final ItemStack EMPTY_BOTTLE = new ItemStack(Items.GLASS_BOTTLE);
    public static final ItemStack WATER_BOTTLE = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);

    private static final String NBT_INVENTORY = "Inventory_%d";
    private static final String NBT_SLOT = "Slot";

    private static final Map<Integer, List<ItemStack>> OREDICT_CACHE = new HashMap<>();
    private static final Map<Integer, Boolean> OREDICT_EQUIVALENCY_CACHE = new HashMap<>();

    private static final NonNullList<Object> EMPTY_NON_NULL_LIST = NonNullList.create();

    public static List<ItemStack> getEquivalentStacks(ItemStack stack) {
        int hash = API.instance().getItemStackHashCode(stack, false);

        if (OREDICT_CACHE.containsKey(hash)) {
            return OREDICT_CACHE.get(hash);
        }

        List<ItemStack> ores = new ArrayList<>();

        for (int id : OreDictionary.getOreIDs(stack)) {
            ores.addAll(OreDictionary.getOres(OreDictionary.getOreName(id)));
        }

        OREDICT_CACHE.put(hash, ores);

        return ores;
    }

    public static boolean areStacksEquivalent(ItemStack left, ItemStack right) {
        int code = API.instance().getItemStackHashCode(left, false);
        code = 31 * code + API.instance().getItemStackHashCode(right, false);

        if (OREDICT_EQUIVALENCY_CACHE.containsKey(code)) {
            return OREDICT_EQUIVALENCY_CACHE.get(code);
        }

        int[] leftIds = OreDictionary.getOreIDs(left);
        int[] rightIds = OreDictionary.getOreIDs(right);

        for (int i : rightIds) {
            if (ArrayUtils.contains(leftIds, i)) {
                OREDICT_EQUIVALENCY_CACHE.put(code, true);

                return true;
            }
        }

        OREDICT_EQUIVALENCY_CACHE.put(code, false);

        return false;
    }

    public static void writeItemStack(ByteBuf buf, ItemStack stack) {
        buf.writeInt(Item.getIdFromItem(stack.getItem()));
        buf.writeInt(stack.getCount());
        buf.writeInt(stack.getItemDamage());
        ByteBufUtils.writeTag(buf, stack.getItem().getNBTShareTag(stack));
    }

    public static ItemStack readItemStack(ByteBuf buf) {
        ItemStack stack = new ItemStack(Item.getItemById(buf.readInt()), buf.readInt(), buf.readInt());
        stack.setTagCompound(ByteBufUtils.readTag(buf));
        return stack;
    }

    public static void writeItemStack(ByteBuf buf, ItemStack stack, @Nullable INetwork network, boolean displayCraftText) {
        writeItemStack(buf, stack);

        buf.writeInt(API.instance().getItemStackHashCode(stack));

        if (network != null) {
            buf.writeBoolean(network.getCraftingManager().hasPattern(stack));
            buf.writeBoolean(displayCraftText);
        } else {
            buf.writeBoolean(false);
            buf.writeBoolean(false);
        }
    }

    public static void writeFluidStack(ByteBuf buf, FluidStack stack) {
        buf.writeInt(API.instance().getFluidStackHashCode(stack));
        ByteBufUtils.writeUTF8String(buf, FluidRegistry.getFluidName(stack.getFluid()));
        buf.writeInt(stack.amount);
        ByteBufUtils.writeTag(buf, stack.tag);
    }

    public static Pair<Integer, FluidStack> readFluidStack(ByteBuf buf) {
        return Pair.of(buf.readInt(), new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf)));
    }

    public static ItemStack nullToEmpty(@Nullable ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Nullable
    public static ItemStack emptyToNull(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? null : stack;
    }

    @SuppressWarnings("unchecked")
    public static void createStorages(ItemStack disk, int slot, IStorageDisk<ItemStack>[] itemStorages, IStorageDisk<FluidStack>[] fluidStorages, Function<IStorageDisk<ItemStack>, IStorageDisk> itemStorageWrapper, Function<IStorageDisk<FluidStack>, IStorageDisk> fluidStorageWrapper) {
        if (disk.isEmpty()) {
            itemStorages[slot] = null;
            fluidStorages[slot] = null;
        } else {
            IStorageDiskProvider provider = (IStorageDiskProvider) disk.getItem();
            IStorageDisk storage = provider.create(disk);

            storage.readFromNBT();

            switch (storage.getType()) {
                case ITEMS:
                    itemStorages[slot] = itemStorageWrapper.apply(storage);
                    break;
                case FLUIDS:
                    fluidStorages[slot] = fluidStorageWrapper.apply(storage);
                    break;
            }
        }
    }

    public static NonNullList<ItemStack> toNonNullList(List<ItemStack> list) {
        NonNullList<ItemStack> other = NonNullList.create();

        for (ItemStack item : list) {
            if (item != null) {
                other.add(item);
            }
        }

        return other;
    }

    @SuppressWarnings("unchecked")
    public static <T> NonNullList<T> emptyNonNullList() {
        return (NonNullList<T>) EMPTY_NON_NULL_LIST;
    }

    public static void writeItems(IItemHandler handler, int id, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();

                stackTag.setInteger(NBT_SLOT, i);

                handler.getStackInSlot(i).writeToNBT(stackTag);

                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(String.format(NBT_INVENTORY, id), tagList);
    }

    public static void readItems(IItemHandlerModifiable handler, int id, NBTTagCompound tag) {
        String name = String.format(NBT_INVENTORY, id);

        if (tag.hasKey(name)) {
            NBTTagList tagList = tag.getTagList(name, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger(NBT_SLOT);

                if (slot >= 0 && slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, new ItemStack(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    public static void writeItems(IInventory inventory, int id, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();

                stackTag.setInteger(NBT_SLOT, i);

                inventory.getStackInSlot(i).writeToNBT(stackTag);

                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(String.format(NBT_INVENTORY, id), tagList);
    }

    public static void readItems(IInventory inventory, int id, NBTTagCompound tag) {
        String name = String.format(NBT_INVENTORY, id);

        if (tag.hasKey(name)) {
            NBTTagList tagList = tag.getTagList(name, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger(NBT_SLOT);

                ItemStack stack = new ItemStack(tagList.getCompoundTagAt(i));

                if (!stack.isEmpty()) {
                    inventory.setInventorySlotContents(slot, stack);
                }
            }
        }
    }

    public static NBTTagList serializeFluidStackList(IStackList<FluidStack> list) {
        NBTTagList tagList = new NBTTagList();

        for (FluidStack stack : list.getStacks()) {
            tagList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }

        return tagList;
    }

    public static IStackList<FluidStack> readFluidStackList(NBTTagList tagList) {
        IStackList<FluidStack> list = API.instance().createFluidStackList();

        for (int i = 0; i < tagList.tagCount(); ++i) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(tagList.getCompoundTagAt(i));

            if (stack != null) {
                list.add(stack, stack.amount);
            }
        }

        return list;
    }

    public static boolean hasFluidBucket(FluidStack stack) {
        return stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == FluidRegistry.LAVA || stack.getFluid().getName().equals("milk") || FluidRegistry.getBucketFluids().contains(stack.getFluid());
    }

    public static FluidStack copy(FluidStack stack, int size) {
        FluidStack copy = stack.copy();
        copy.amount = size;
        return copy;
    }

    public static FluidStack copy(@Nullable FluidStack stack) {
        return stack == null ? null : stack.copy();
    }

    public static Pair<ItemStack, FluidStack> getFluid(ItemStack stack, boolean simulate) {
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

            FluidStack result = fluidHandler.drain(Fluid.BUCKET_VOLUME, !simulate);

            return Pair.of(fluidHandler.getContainer(), result);
        }

        return Pair.of(null, null);
    }
}
