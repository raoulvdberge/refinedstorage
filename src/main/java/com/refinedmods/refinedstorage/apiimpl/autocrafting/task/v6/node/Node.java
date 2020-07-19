package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.google.common.primitives.Ints;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.SerializationUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Node {
    private static final String NBT_PATTERN = "Pattern";
    private static final String NBT_ROOT = "Root";
    private static final String NBT_IS_PROCESSING = "IsProcessing";
    private static final String NBT_ITEMS_TO_USE = "ItemsToUse";
    private static final String NBT_QUANTITY = "Quantity";
    private static final String NBT_NEEDED_PER_CRAFT = "NeededPerCraft";

    private final boolean root;
    private final ICraftingPattern pattern;

    protected int quantity;
    protected final Map<Integer, IStackList<ItemStack>> itemRequirements = new LinkedHashMap<>();

    private final Map<Integer, Integer> itemsNeededPerCraft = new LinkedHashMap<>();

    public Node(ICraftingPattern pattern, boolean root) {
        this.pattern = pattern;
        this.root = root;
    }

    public Node(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        this.quantity = tag.getInt(NBT_QUANTITY);
        this.pattern = SerializationUtil.readPatternFromNbt(tag.getCompound(NBT_PATTERN), network.getWorld());
        this.root = tag.getBoolean(NBT_ROOT);

        ListNBT list = tag.getList(NBT_ITEMS_TO_USE, Constants.NBT.TAG_LIST);
        for (int i = 0; i < list.size(); i++) {
            this.itemRequirements.put(i, SerializationUtil.readItemStackList(list.getList(i)));
        }

        List<Integer> perCraftList = Ints.asList(tag.getIntArray(NBT_NEEDED_PER_CRAFT));
        for (int i = 0; i < perCraftList.size(); i++) {
            itemsNeededPerCraft.put(i, perCraftList.get(i));
        }
    }

    public static Node fromNbt(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        return tag.getBoolean(NBT_IS_PROCESSING) ? new ProcessingNode(network, tag) : new CraftingNode(network, tag);
    }

    public abstract void update(INetwork network, int ticks, NodeList nodes, IStorageDisk<ItemStack> internalStorage, IStorageDisk<FluidStack> internalFluidStorage, Runnable onFinishedStep);

    public abstract void onCalculationFinished();

    public ICraftingPattern getPattern() {
        return pattern;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    protected void next() {
        quantity--;
    }

    public boolean isRoot() {
        return root;
    }

    protected IStackList<ItemStack> getItemRequirementsForSingleCraft(boolean simulate) {
        IStackList<ItemStack> toReturn = API.instance().createItemStackList();

        for (int i = 0; i < itemRequirements.size(); i++) {
            int needed = itemsNeededPerCraft.get(i);

            if (!itemRequirements.get(i).isEmpty()) {
                Iterator<StackListEntry<ItemStack>> it = itemRequirements.get(i).getStacks().iterator();

                while (needed > 0 && it.hasNext()) {
                    ItemStack toUse = it.next().getStack();

                    if (needed < toUse.getCount()) {
                        if (!simulate) {
                            itemRequirements.get(i).remove(toUse, needed);
                        }

                        toReturn.add(toUse, needed);

                        needed = 0;
                    } else {
                        if (!simulate) {
                            it.remove();
                        }

                        toReturn.add(toUse);

                        needed -= toUse.getCount();
                    }
                }
            } else { // TODO why break here?
                LogManager.getLogger(Node.class).warn("Craft requested more Items than available"); // TODO Improve logging
                this.quantity = 0; // stop crafting
                break;
            }
        }

        return toReturn;
    }

    public void addItemsToUse(int ingredientNumber, ItemStack stack, int size, int perCraft) {
        if (!itemsNeededPerCraft.containsKey(ingredientNumber)) {
            itemsNeededPerCraft.put(ingredientNumber, perCraft);
        }

        IStackList<ItemStack> list = itemRequirements.get(ingredientNumber);
        if (list == null) {
            itemRequirements.put(ingredientNumber, list = API.instance().createItemStackList());
        }

        list.add(stack, size);
    }

    public CompoundNBT writeToNbt() {
        CompoundNBT tag = new CompoundNBT();

        tag.putInt(NBT_QUANTITY, quantity);
        tag.putBoolean(NBT_IS_PROCESSING, this instanceof ProcessingNode);
        tag.putBoolean(NBT_ROOT, root);
        tag.put(NBT_PATTERN, SerializationUtil.writePatternToNbt(pattern));

        ListNBT itemsToUse = new ListNBT();
        for (IStackList<ItemStack> stackList : this.itemRequirements.values()) {
            itemsToUse.add(SerializationUtil.writeItemStackList(stackList));
        }
        tag.put(NBT_ITEMS_TO_USE, itemsToUse);

        tag.putIntArray(NBT_NEEDED_PER_CRAFT, Ints.toArray(itemsNeededPerCraft.values()));

        return tag;
    }
}