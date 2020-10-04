package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingPatternInputs;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.Ingredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

public class NodeRequirements {
    private static final String NBT_ITEMS_TO_USE = "ItemsToUse";
    private static final String NBT_FLUIDS_TO_USE = "FluidsToUse";

    private static final String NBT_INGREDIENT = "Ingredient";
    private static final String NBT_INGREDIENT_ID = "ID";


    private final Map<Integer, Ingredient<ItemStack>> itemRequirements = new LinkedHashMap<>();
    private final Map<Integer, Ingredient<FluidStack>> fluidRequirements = new LinkedHashMap<>();
    private int maxItemSlot;
    private int maxFluidSlot;

    public NodeRequirements(CraftingPatternInputs inputs) {
        int id = 0;
        for (Ingredient<ItemStack> itemIngredient : inputs.getItemIngredients()) {
            itemRequirements.put(id++, itemIngredient);
        }

        id = 0;
        for (Ingredient<FluidStack> fluidIngredient : inputs.getFluidIngredients()) {
            fluidRequirements.put(id++, fluidIngredient);
        }
    }

    public NodeRequirements(CompoundNBT tag) throws CraftingTaskReadException {
        ListNBT itemNbt = tag.getList(NBT_ITEMS_TO_USE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemNbt.size(); i++) {
            itemRequirements.put(itemNbt.getCompound(i).getInt(NBT_INGREDIENT_ID), new Ingredient<>(true, itemNbt.getCompound(i).getCompound(NBT_INGREDIENT)));
        }

        ListNBT fluidNbt = tag.getList(NBT_FLUIDS_TO_USE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluidNbt.size(); i++) {
            fluidRequirements.put(fluidNbt.getCompound(i).getInt(NBT_INGREDIENT_ID), new Ingredient<>(false, fluidNbt.getCompound(i).getCompound(NBT_INGREDIENT)));
        }

        readMaxSlots();
    }

    public void readMaxSlots() {
        for (Ingredient<ItemStack> ingredient : itemRequirements.values()) {
            maxItemSlot = Math.max(maxItemSlot, ingredient.getSlotCounts().keySet().stream().max(Integer::compareTo).get());
        }

        for (Ingredient<FluidStack> ingredient : fluidRequirements.values()) {
            maxFluidSlot = Math.max(maxFluidSlot, ingredient.getSlotCounts().keySet().stream().max(Integer::compareTo).get());
        }
    }

    public void addItemRequirement(int ingredientNumber, ItemStack stack, int size, int perCraft) {
        IStackList<ItemStack> list = itemRequirements.get(ingredientNumber).getIngredients();
        if (list == null) {
            list = API.instance().createItemStackList();
        }

        list.add(stack, size);
    }

    public void addFluidRequirement(int ingredientNumber, FluidStack stack, int size, int perCraft) {
        IStackList<FluidStack> list = fluidRequirements.get(ingredientNumber).getIngredients();
        if (list == null) {
            list = API.instance().createFluidStackList();
        }

        list.add(stack, size);
    }

    Map<Integer, IStackList<ItemStack>> getSingleItemRequirementSet(boolean simulate) {
        Map<Integer, IStackList<ItemStack>> map = new HashMap<>();
        itemRequirements.forEach((x, y) -> map.put(x, y.getIngredientsForSingleCraft(simulate)));
        return map;
    }

    Map<Integer, IStackList<FluidStack>> getSingleFluidRequirementSet(boolean simulate) {
        Map<Integer, IStackList<FluidStack>> map = new HashMap<>();
        fluidRequirements.forEach((x, y) -> map.put(x, y.getIngredientsForSingleCraft(simulate)));
        return map;
    }

    NonNullList<ItemStack> getItemsAsList(Map<Integer, Queue<ItemStack>> extracted, boolean removeEmpty) {
        if (extracted.isEmpty()) {
            return NonNullList.create();
        }

        NonNullList<ItemStack> toReturn = NonNullList.withSize(maxItemSlot + 1, ItemStack.EMPTY);
        extracted.forEach((id, queue) -> itemRequirements.get(id).getSlotCounts().forEach((slot, count) -> {
            int needed = count;
            boolean first = true;
            while (needed > 0) {
                if (queue.isEmpty()) {
                    throw new IllegalStateException("Recipe requires more items than extracted");
                }

                ItemStack queueStack = queue.peek();
                ItemStack stack = queueStack.copy();
                if (stack.getCount() > needed) {
                    stack.setCount(needed);
                    queueStack.setCount(queueStack.getCount() - needed);
                    needed = 0;
                } else {
                    needed -= stack.getCount();
                    queue.poll();
                }

                if (first) {
                    toReturn.set(slot, stack);
                    first = false;
                } else {
                    // if 2 items need to go into the same slot squeeze it in
                    toReturn.add(slot, stack);
                }
            }
        }));
        if (removeEmpty) {
            toReturn.removeIf(ItemStack::isEmpty);
        }

        return toReturn;
    }

    NonNullList<FluidStack> getFluidsAsList(Map<Integer, Queue<FluidStack>> extracted) {
        if (extracted.isEmpty()) {
            return NonNullList.create();
        }

        NonNullList<FluidStack> toReturn = NonNullList.withSize(maxFluidSlot + 1, FluidStack.EMPTY);
        extracted.forEach((id, queue) -> fluidRequirements.get(id).getSlotCounts().forEach((slot, count) -> {
            int needed = count;
            boolean first = true;
            while (needed > 0) {
                if (queue.isEmpty()) {
                    throw new IllegalStateException("Recipe requires more fluids than extracted");
                }

                FluidStack queueStack = queue.peek();
                FluidStack stack = queueStack.copy();
                if (stack.getAmount() > needed) {
                    stack.setAmount(needed);
                    queueStack.setAmount(queueStack.getAmount() - needed);
                    needed = 0;
                } else {
                    needed -= stack.getAmount();
                    queue.poll();
                }

                if (first) {
                    toReturn.set(slot, stack);
                    first = false;
                } else {
                    // if 2 fluids need to go into the same slot squeeze it in
                    toReturn.add(slot + 1, stack);
                }
            }
        }));
        toReturn.removeIf(FluidStack::isEmpty);
        return toReturn;
    }

    public CompoundNBT writeToNbt(CompoundNBT tag) {
        ListNBT itemRequirements = new ListNBT();
        this.itemRequirements.forEach((key, value) -> {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt(NBT_INGREDIENT_ID, key);
            nbt.put(NBT_INGREDIENT, value.writeToNbt());
        });
        tag.put(NBT_ITEMS_TO_USE, itemRequirements);

        ListNBT fluidRequirements = new ListNBT();
        this.fluidRequirements.forEach((key, value) -> {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt(NBT_INGREDIENT_ID, key);
            nbt.put(NBT_INGREDIENT, value.writeToNbt());
        });
        tag.put(NBT_FLUIDS_TO_USE, fluidRequirements);

        return tag;
    }
}
