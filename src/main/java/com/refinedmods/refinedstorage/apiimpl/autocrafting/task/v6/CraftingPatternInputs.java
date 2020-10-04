package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingPatternInputs {
    private final NonNullList<ItemStack> recipe = NonNullList.create();
    private final List<Ingredient<ItemStack>> itemIngredients = new ArrayList<>();
    private final List<Ingredient<FluidStack>> fluidIngredients = new ArrayList<>();

    public CraftingPatternInputs(ICraftingPattern pattern) {
        fillOutRecipe(pattern);
        combineItemInputs(pattern);
        combineFluidInputs(pattern);
    }

    private void fillOutRecipe(ICraftingPattern pattern) {
        for (NonNullList<ItemStack> inputsForSlot : pattern.getInputs()) {
            if (inputsForSlot.isEmpty()) {
                recipe.add(ItemStack.EMPTY);
            } else {
                recipe.add(inputsForSlot.get(0));
            }
        }
    }

    private void combineItemInputs(ICraftingPattern pattern) {
        for (int i = 0; i < pattern.getInputs().size(); i++) {
            NonNullList<ItemStack> inputsForSlot = pattern.getInputs().get(i);
            if (inputsForSlot.isEmpty()) {
                continue;
            }

            Ingredient<ItemStack> matchingIngredient = findMatchingItemIngredient(inputsForSlot);

            if (matchingIngredient == null) {
                itemIngredients.add(new Ingredient<>(inputsForSlot, inputsForSlot.get(0).getCount(), i));
            } else {
                matchingIngredient.increaseCount(inputsForSlot.get(0).getCount(), i);
            }
        }
    }

    private void combineFluidInputs(ICraftingPattern pattern) {
        for (int i = 0; i < pattern.getFluidInputs().size(); i++) {
            NonNullList<FluidStack> inputsForSlot = pattern.getFluidInputs().get(i);
            if (inputsForSlot.isEmpty()) {
                continue;
            }

            Ingredient<FluidStack> matchingIngredient = findMatchingFluidIngredient(inputsForSlot);

            if (matchingIngredient == null) {
                fluidIngredients.add(new Ingredient<>(inputsForSlot, inputsForSlot.get(0).getAmount(), i));
            } else {
                matchingIngredient.increaseCount(inputsForSlot.get(0).getAmount(), i);
            }
        }
    }

    @Nullable
    private Ingredient<ItemStack> findMatchingItemIngredient(NonNullList<ItemStack> inputsForSlot) {
        for (Ingredient<ItemStack> existingIngredient : itemIngredients) {
            if (existingIngredient.getInputs().size() == inputsForSlot.size()) {
                boolean found = true;

                for (int i = 0; i < inputsForSlot.size(); i++) {
                    if (!API.instance().getComparer().isEqualNoQuantity(existingIngredient.getInputs().get(i), inputsForSlot.get(i))) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    return existingIngredient;
                }
            }
        }

        return null;
    }

    @Nullable
    private Ingredient<FluidStack> findMatchingFluidIngredient(NonNullList<FluidStack> inputsForSlot) {
        for (Ingredient<FluidStack> existingIngredient : fluidIngredients) {
            if (existingIngredient.getInputs().size() == inputsForSlot.size()) {
                boolean found = true;

                for (int i = 0; i < inputsForSlot.size(); i++) {
                    if (!API.instance().getComparer().isEqual(existingIngredient.getInputs().get(i), inputsForSlot.get(i), IComparer.COMPARE_NBT)) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    return existingIngredient;
                }
            }
        }

        return null;
    }

    public NonNullList<ItemStack> getRecipe() {
        return recipe;
    }

    public List<Ingredient<ItemStack>> getItemIngredients() {
        return itemIngredients;
    }

    public List<Ingredient<FluidStack>> getFluidIngredients() {
        return fluidIngredients;
    }
}
