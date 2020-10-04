package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6;

import com.google.common.primitives.Ints;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ingredient<T> {
    private static final String NBT_INGREDIENTS = "Ingredients";
    private static final String NBT_AMOUNTS_PER_SLOT = "AmountsPerSlot";
    private static final String NBT_COUNT = "Count";

    private NonNullList<T> inputs; //Not written to NBT, only used for Calculation.
    private final IStackList<T> ingredients;
    private int count;
    private final boolean isItem;
    private final Map<Integer, Integer> slotCounts = new HashMap<>();

    public Ingredient(NonNullList<T> inputs, int count, int slot) {
        this.inputs = inputs;
        this.count = count;
        this.slotCounts.put(slot, count);
        this.isItem = inputs.get(0) instanceof ItemStack;
        this.ingredients = getStackList();
    }

    public Ingredient(boolean isItem, CompoundNBT nbt) throws CraftingTaskReadException {
        this.isItem = isItem;
        this.count = nbt.getInt(NBT_COUNT);
        if (isItem) {
            this.ingredients = (IStackList<T>) SerializationUtil.readItemStackList(nbt.getList(NBT_INGREDIENTS, Constants.NBT.TAG_COMPOUND));
        } else {
            this.ingredients = (IStackList<T>) SerializationUtil.readFluidStackList(nbt.getList(NBT_INGREDIENTS, Constants.NBT.TAG_COMPOUND));
        }

        List<Integer> ints = Ints.asList(nbt.getIntArray(NBT_AMOUNTS_PER_SLOT));
        for (int i = 0; i < ints.size(); i += 2) {
            this.slotCounts.put(ints.get(i), ints.get(i + 1));
        }
    }

    private IStackList<T> getStackList() {
        return isItem ? (IStackList<T>) API.instance().createItemStackList()
            : (IStackList<T>) API.instance().createFluidStackList();
    }

    public NonNullList<T> getInputs() {
        return inputs;
    }

    public int getCount() {
        return count;
    }

    public void increaseCount(int count, int slot) {
        this.count += count;
        this.slotCounts.put(slot, count);
    }

    public Map<Integer, Integer> getSlotCounts() {
        return slotCounts;
    }

    public IStackList<T> getIngredients() {
        return ingredients;
    }

    public IStackList<T> getIngredientsForSingleCraft(boolean simulate) {
        if (ingredients.isEmpty()) {
            LogManager.getLogger(Ingredient.class).warn("Craft requested more Items than available");
            return getStackList();
        }
        int amount = getCount();
        IStackList<T> list = getStackList();
        Map<T, Integer> toRemove = new HashMap<>();
        for (StackListEntry<T> stackListEntry : ingredients.getStacks()) {
            T stack = stackListEntry.getStack();
            int needed = Math.min(isItem ? ((ItemStack) stack).getCount() : ((FluidStack) stack).getAmount(), amount);
            if (!simulate) {
                toRemove.put(stack, needed);
            }
            list.add(stack, needed);
            amount -= needed;
            if (amount <= 0) break;
        }
        toRemove.forEach(ingredients::remove);
        return list;
    }

    public INBT writeToNbt() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(NBT_COUNT, count);
        if (isItem) {
            tag.put(NBT_INGREDIENTS, SerializationUtil.writeItemStackList((IStackList<ItemStack>) ingredients));
        } else {
            tag.put(NBT_INGREDIENTS, SerializationUtil.writeFluidStackList((IStackList<FluidStack>) ingredients));
        }

        List<Integer> list = new ArrayList<>();
        slotCounts.forEach((x, y) -> {
            list.add(x);
            list.add(y);
        });

        tag.putIntArray(NBT_AMOUNTS_PER_SLOT, list);
        return tag;
    }


}