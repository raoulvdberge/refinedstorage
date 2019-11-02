package com.raoulvdberge.refinedstorage.apiimpl.autocrafting;

import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.registry.CraftingTaskFactory;
import com.raoulvdberge.refinedstorage.item.PatternItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingPattern implements ICraftingPattern {
    private ICraftingPatternContainer container;
    private ItemStack stack;
    private boolean processing;
    private boolean exact;
    private boolean valid;
    private ICraftingRecipe recipe;
    private List<NonNullList<ItemStack>> inputs = new ArrayList<>();
    private NonNullList<ItemStack> outputs = NonNullList.create();
    private NonNullList<ItemStack> byproducts = NonNullList.create();
    private List<NonNullList<FluidStack>> fluidInputs = new ArrayList<>();
    private NonNullList<FluidStack> fluidOutputs = NonNullList.create();

    public CraftingPattern(World world, ICraftingPatternContainer container, ItemStack stack) {
        this.container = container;
        this.stack = stack;
        this.processing = PatternItem.isProcessing(stack);
        this.exact = PatternItem.isExact(stack);

        if (processing) {
            for (int i = 0; i < 9; ++i) {
                initProcessingItems(i);
                initProcessingFluids(i);
            }
        } else {
            CraftingInventory inv = new DummyCraftingInventory();

            for (int i = 0; i < 9; ++i) {
                initCraftingItemInputs(inv, i);
            }

            Optional<ICraftingRecipe> foundRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world);
            if (foundRecipe.isPresent()) {
                this.recipe = foundRecipe.get();

                this.byproducts = recipe.getRemainingItems(inv);

                ItemStack output = recipe.getCraftingResult(inv);

                if (!output.isEmpty()) {
                    this.valid = true;

                    outputs.add(output);

                    if (!exact) {
                        modifyCraftingItemInputsToUtiliseTags();
                    }
                }
            }
        }
    }

    private void initProcessingItems(int i) {
        ItemStack input = PatternItem.getInputSlot(stack, i);

        if (input.isEmpty()) {
            inputs.add(NonNullList.create());
        } else if (!exact) {
            NonNullList<ItemStack> possibilities = NonNullList.create();

            possibilities.add(input.copy());

            for (ResourceLocation owningTag : ItemTags.getCollection().getOwningTags(input.getItem())) {
                for (Item element : ItemTags.getCollection().get(owningTag).getAllElements()) {
                    possibilities.add(new ItemStack(element, input.getCount()));
                }
            }

            inputs.add(possibilities);
        } else {
            inputs.add(NonNullList.from(ItemStack.EMPTY, input));
        }

        ItemStack output = PatternItem.getOutputSlot(stack, i);
        if (!output.isEmpty()) {
            this.valid = true;

            outputs.add(output);
        }
    }

    private void initProcessingFluids(int i) {
        FluidStack fluidInput = PatternItem.getFluidInputSlot(stack, i);
        if (fluidInput.isEmpty()) {
            fluidInputs.add(NonNullList.create());
        } else if (!exact) {
            NonNullList<FluidStack> possibilities = NonNullList.create();

            possibilities.add(fluidInput.copy());

            for (ResourceLocation owningTag : FluidTags.getCollection().getOwningTags(fluidInput.getFluid())) {
                for (Fluid element : FluidTags.getCollection().get(owningTag).getAllElements()) {
                    possibilities.add(new FluidStack(element, fluidInput.getAmount()));
                }
            }

            fluidInputs.add(possibilities);
        } else {
            fluidInputs.add(NonNullList.from(FluidStack.EMPTY, fluidInput));
        }

        FluidStack fluidOutput = PatternItem.getFluidOutputSlot(stack, i);
        if (!fluidOutput.isEmpty()) {
            this.valid = true;

            fluidOutputs.add(fluidOutput);
        }
    }

    private void initCraftingItemInputs(CraftingInventory inv, int i) {
        ItemStack input = PatternItem.getInputSlot(stack, i);

        inputs.add(input.isEmpty() ? NonNullList.create() : NonNullList.from(ItemStack.EMPTY, input));

        inv.setInventorySlotContents(i, input);
    }

    private void modifyCraftingItemInputsToUtiliseTags() {
        if (recipe.getIngredients().size() > 0) {
            inputs.clear();

            for (int i = 0; i < recipe.getIngredients().size(); ++i) {
                inputs.add(i, NonNullList.from(ItemStack.EMPTY, recipe.getIngredients().get(i).getMatchingStacks()));
            }
        } else {
            this.valid = false;
        }
    }

    @Override
    public ICraftingPatternContainer getContainer() {
        return container;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isProcessing() {
        return processing;
    }

    @Override
    public List<NonNullList<ItemStack>> getInputs() {
        return inputs;
    }

    @Override
    public NonNullList<ItemStack> getOutputs() {
        return outputs;
    }

    public ItemStack getOutput(NonNullList<ItemStack> took) {
        if (processing) {
            throw new IllegalStateException("Cannot get crafting output from processing pattern");
        }

        if (took.size() != inputs.size()) {
            throw new IllegalArgumentException("The items that are taken (" + took.size() + ") should match the inputs for this pattern (" + inputs.size() + ")");
        }

        CraftingInventory inv = new DummyCraftingInventory();

        for (int i = 0; i < took.size(); ++i) {
            inv.setInventorySlotContents(i, took.get(i));
        }

        ItemStack result = recipe.getCraftingResult(inv);
        if (result.isEmpty()) {
            throw new IllegalStateException("Cannot have empty result");
        }

        return result;
    }

    @Override
    public NonNullList<ItemStack> getByproducts() {
        if (processing) {
            throw new IllegalStateException("Cannot get byproduct outputs from processing pattern");
        }

        return byproducts;
    }

    @Override
    public NonNullList<ItemStack> getByproducts(NonNullList<ItemStack> took) {
        if (processing) {
            throw new IllegalStateException("Cannot get byproduct outputs from processing pattern");
        }

        if (took.size() != inputs.size()) {
            throw new IllegalArgumentException("The items that are taken (" + took.size() + ") should match the inputs for this pattern (" + inputs.size() + ")");
        }

        CraftingInventory inv = new DummyCraftingInventory();

        for (int i = 0; i < took.size(); ++i) {
            inv.setInventorySlotContents(i, took.get(i));
        }

        NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(inv);
        NonNullList<ItemStack> sanitized = NonNullList.create();

        for (ItemStack item : remainingItems) {
            if (!item.isEmpty()) {
                sanitized.add(item);
            }
        }

        return sanitized;
    }

    @Override
    public List<NonNullList<FluidStack>> getFluidInputs() {
        return fluidInputs;
    }

    @Override
    public NonNullList<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    @Override
    public ResourceLocation getId() {
        return CraftingTaskFactory.ID;
    }

    @Override
    public boolean canBeInChainWith(ICraftingPattern other) {
        if (other.isProcessing() != processing) {
            return false;
        }

        if ((other.getInputs().size() != inputs.size()) ||
            (other.getFluidInputs().size() != fluidInputs.size()) ||
            (other.getOutputs().size() != outputs.size()) ||
            (other.getFluidOutputs().size() != fluidOutputs.size())) {
            return false;
        }

        if (!processing && other.getByproducts().size() != byproducts.size()) {
            return false;
        }

        for (int i = 0; i < inputs.size(); ++i) {
            List<ItemStack> inputs = this.inputs.get(i);
            List<ItemStack> otherInputs = other.getInputs().get(i);

            if (inputs.size() != otherInputs.size()) {
                return false;
            }

            for (int j = 0; j < inputs.size(); ++j) {
                if (!API.instance().getComparer().isEqual(inputs.get(j), otherInputs.get(j))) {
                    return false;
                }
            }
        }

        for (int i = 0; i < fluidInputs.size(); ++i) {
            List<FluidStack> inputs = this.fluidInputs.get(i);
            List<FluidStack> otherInputs = other.getFluidInputs().get(i);

            if (inputs.size() != otherInputs.size()) {
                return false;
            }

            for (int j = 0; j < inputs.size(); ++j) {
                if (!API.instance().getComparer().isEqual(inputs.get(j), otherInputs.get(j), IComparer.COMPARE_NBT | IComparer.COMPARE_QUANTITY)) {
                    return false;
                }
            }
        }

        for (int i = 0; i < outputs.size(); ++i) {
            if (!API.instance().getComparer().isEqual(outputs.get(i), other.getOutputs().get(i))) {
                return false;
            }
        }

        for (int i = 0; i < fluidOutputs.size(); ++i) {
            if (!API.instance().getComparer().isEqual(fluidOutputs.get(i), other.getFluidOutputs().get(i), IComparer.COMPARE_NBT | IComparer.COMPARE_QUANTITY)) {
                return false;
            }
        }

        if (!processing) {
            for (int i = 0; i < byproducts.size(); ++i) {
                if (!API.instance().getComparer().isEqual(byproducts.get(i), other.getByproducts().get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int getChainHashCode() {
        int result = 0;

        result = 31 * result + (processing ? 1 : 0);
        result = 31 * result + (exact ? 1 : 0);

        for (List<ItemStack> inputs : this.inputs) {
            for (ItemStack input : inputs) {
                result = 31 * result + API.instance().getItemStackHashCode(input);
            }
        }

        for (List<FluidStack> inputs : this.fluidInputs) {
            for (FluidStack input : inputs) {
                result = 31 * result + API.instance().getFluidStackHashCode(input);
            }
        }

        for (ItemStack output : this.outputs) {
            result = 31 * result + API.instance().getItemStackHashCode(output);
        }

        for (FluidStack output : this.fluidOutputs) {
            result = 31 * result + API.instance().getFluidStackHashCode(output);
        }

        for (ItemStack byproduct : this.byproducts) {
            result = 31 * result + API.instance().getItemStackHashCode(byproduct);
        }

        return result;
    }

    private class DummyCraftingInventory extends CraftingInventory {
        public DummyCraftingInventory() {
            super(new Container(null, 0) {
                @Override
                public boolean canInteractWith(PlayerEntity player) {
                    return true;
                }
            }, 3, 3);
        }
    }
}
