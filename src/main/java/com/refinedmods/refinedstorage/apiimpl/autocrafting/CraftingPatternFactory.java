package com.refinedmods.refinedstorage.apiimpl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.item.PatternItem;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public class CraftingPatternFactory {
    public static final CraftingPatternFactory INSTANCE = new CraftingPatternFactory();

    public ICraftingPattern create(World world, ICraftingPatternContainer container, ItemStack stack) {
        CraftingPatternContext context = new CraftingPatternContext(container, stack);

        boolean processing = PatternItem.isProcessing(stack);
        boolean exact = PatternItem.isExact(stack);
        AllowedTagList allowedTagList = PatternItem.getAllowedTags(stack);

        List<NonNullList<ItemStack>> inputs = new ArrayList<>();
        NonNullList<ItemStack> outputs = NonNullList.create();
        NonNullList<ItemStack> byproducts = NonNullList.create();
        List<NonNullList<FluidStack>> fluidInputs = new ArrayList<>();
        NonNullList<FluidStack> fluidOutputs = NonNullList.create();
        ICraftingRecipe recipe = null;

        try {
            if (processing) {
                for (int i = 0; i < 9; ++i) {
                    fillProcessingInputs(i, stack, inputs, outputs, allowedTagList);
                    fillProcessingFluidInputs(i, stack, fluidInputs, fluidOutputs, allowedTagList);
                }

                if (outputs.isEmpty() && fluidOutputs.isEmpty()) {
                    throw new CraftingPatternFactoryException(new TranslationTextComponent("misc.refinedstorage.pattern.error.processing_no_outputs"));
                }
            } else {
                CraftingInventory inv = new CraftingPattern.DummyCraftingInventory(context);

                for (int i = 0; i < 9; ++i) {
                    fillCraftingInputs(inv, stack, inputs, i);
                }

                Optional<ICraftingRecipe> foundRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world);
                if (foundRecipe.isPresent()) {
                    recipe = foundRecipe.get();

                    byproducts = recipe.getRemainingItems(inv);

                    ItemStack output = recipe.getCraftingResult(inv);

                    if (!output.isEmpty()) {
                        outputs.add(output);

                        if (!exact) {
                            modifyCraftingInputsToUseAlternatives(recipe, inputs);
                        }
                    } else {
                        throw new CraftingPatternFactoryException(new TranslationTextComponent("misc.refinedstorage.pattern.error.no_output"));
                    }
                } else {
                    throw new CraftingPatternFactoryException(new TranslationTextComponent("misc.refinedstorage.pattern.error.recipe_does_not_exist"));
                }
            }
        } catch (CraftingPatternFactoryException e) {
            return new InvalidCraftingPattern(context, e.getErrorMessage());
        }

        return new CraftingPattern(
            context,
            processing,
            exact,
            recipe,
            new CraftingPatternInputs(inputs, fluidInputs),
            new CraftingPatternOutputs(outputs, byproducts, fluidOutputs),
            allowedTagList
        );
    }

    private void fillProcessingInputs(int i, ItemStack stack, List<NonNullList<ItemStack>> inputs, NonNullList<ItemStack> outputs, @Nullable AllowedTagList allowedTagList) throws CraftingPatternFactoryException {
        ItemStack input = PatternItem.getInputSlot(stack, i);

        if (input.isEmpty()) {
            inputs.add(NonNullList.create());
        } else {
            NonNullList<ItemStack> possibilities = NonNullList.create();

            possibilities.add(input.copy());

            if (allowedTagList != null) {
                Collection<ResourceLocation> tagsOfItem = ItemTags.getCollection().getOwningTags(input.getItem());
                Set<ResourceLocation> declaredAllowedTags = allowedTagList.getAllowedItemTags().get(i);

                for (ResourceLocation declaredAllowedTag : declaredAllowedTags) {
                    if (!tagsOfItem.contains(declaredAllowedTag)) {
                        throw new CraftingPatternFactoryException(
                            new TranslationTextComponent(
                                "misc.refinedstorage.pattern.error.tag_no_longer_applicable",
                                declaredAllowedTag.toString(),
                                input.getDisplayName()
                            )
                        );
                    } else {
                        for (Item element : ItemTags.getCollection().get(declaredAllowedTag).getAllElements()) {
                            possibilities.add(new ItemStack(element, input.getCount()));
                        }
                    }
                }
            }

            inputs.add(possibilities);
        }

        ItemStack output = PatternItem.getOutputSlot(stack, i);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    private void fillProcessingFluidInputs(int i, ItemStack stack, List<NonNullList<FluidStack>> fluidInputs, NonNullList<FluidStack> fluidOutputs, @Nullable AllowedTagList allowedTagList) throws CraftingPatternFactoryException {
        FluidStack input = PatternItem.getFluidInputSlot(stack, i);
        if (input.isEmpty()) {
            fluidInputs.add(NonNullList.create());
        } else {
            NonNullList<FluidStack> possibilities = NonNullList.create();

            possibilities.add(input.copy());

            if (allowedTagList != null) {
                Collection<ResourceLocation> tagsOfFluid = FluidTags.getCollection().getOwningTags(input.getFluid());
                Set<ResourceLocation> declaredAllowedTags = allowedTagList.getAllowedFluidTags().get(i);

                for (ResourceLocation declaredAllowedTag : declaredAllowedTags) {
                    if (!tagsOfFluid.contains(declaredAllowedTag)) {
                        throw new CraftingPatternFactoryException(
                            new TranslationTextComponent(
                                "misc.refinedstorage.pattern.error.tag_no_longer_applicable",
                                declaredAllowedTag.toString(),
                                input.getDisplayName()
                            )
                        );
                    } else {
                        for (Fluid element : FluidTags.getCollection().get(declaredAllowedTag).getAllElements()) {
                            possibilities.add(new FluidStack(element, input.getAmount()));
                        }
                    }
                }
            }

            fluidInputs.add(possibilities);
        }

        FluidStack output = PatternItem.getFluidOutputSlot(stack, i);
        if (!output.isEmpty()) {
            fluidOutputs.add(output);
        }
    }

    private void fillCraftingInputs(CraftingInventory inv, ItemStack stack, List<NonNullList<ItemStack>> inputs, int i) {
        ItemStack input = PatternItem.getInputSlot(stack, i);

        inputs.add(input.isEmpty() ? NonNullList.create() : NonNullList.from(ItemStack.EMPTY, input));

        inv.setInventorySlotContents(i, input);
    }

    private void modifyCraftingInputsToUseAlternatives(ICraftingRecipe recipe, List<NonNullList<ItemStack>> inputs) {
        if (!recipe.getIngredients().isEmpty()) {
            inputs.clear();

            for (int i = 0; i < recipe.getIngredients().size(); ++i) {
                inputs.add(i, NonNullList.from(ItemStack.EMPTY, recipe.getIngredients().get(i).getMatchingStacks()));
            }
        }
    }
}
