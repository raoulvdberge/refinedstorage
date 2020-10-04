package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.calculator;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.preview.ICraftingPreviewElement;
import com.refinedmods.refinedstorage.api.autocrafting.task.CalculationResultType;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICalculationResult;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICraftingRequestInfo;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingPatternInputs;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingTask;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.Ingredient;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node.CraftingNode;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node.Node;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node.NodeList;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node.ProcessingNode;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.preview.CraftingPreviewElementFactory;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.preview.CraftingPreviewInfo;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CraftingCalculator {
    private final INetwork network;
    private final ICraftingRequestInfo requested;
    private final int quantity;
    private final ICraftingPattern pattern;

    private final Set<ICraftingPattern> patternsUsed = new HashSet<>();

    private final CraftingPreviewInfo craftingPreviewInfo = new CraftingPreviewInfo();

    private final NodeList nodes = new NodeList();

    private final IStackList<ItemStack> toExtractInitial = API.instance().createItemStackList();
    private final IStackList<FluidStack> toExtractInitialFluids = API.instance().createFluidStackList();

    private long calculationStarted = -1;

    public CraftingCalculator(INetwork network, ICraftingRequestInfo requested, int quantity, ICraftingPattern pattern) {
        this.network = network;
        this.requested = requested;
        this.quantity = quantity;
        this.pattern = pattern;
    }

    public ICalculationResult calculate() {
        this.calculationStarted = System.currentTimeMillis();

        IStackList<ItemStack> results = API.instance().createItemStackList();
        IStackList<FluidStack> fluidResults = API.instance().createFluidStackList();

        IStackList<ItemStack> storageSource = network.getItemStorageCache().getList().copy();
        IStackList<FluidStack> fluidStorageSource = network.getFluidStorageCache().getList().copy();

        int qtyPerCraft = getQuantityPerCraft(requested.getItem(), requested.getFluid(), pattern);
        int qty = ((quantity - 1) / qtyPerCraft) + 1;

        try {
            calculateInternal(qty, storageSource, fluidStorageSource, results, fluidResults, pattern, true);
        } catch (CraftingCalculatorException e) {
            return new CalculationResult(e.getType(), e.getRecursedPattern());
        }

        if (requested.getItem() != null) {
            craftingPreviewInfo.getToCraft().add(ItemHandlerHelper.copyStackWithSize(requested.getItem(), qty * qtyPerCraft));
        } else if (requested.getFluid() != null) {
            craftingPreviewInfo.getToCraftFluids().add(StackUtils.copy(requested.getFluid(), qty * qtyPerCraft));
        }

        List<ICraftingPreviewElement<?>> previewElements = new CraftingPreviewElementFactory().getElements(craftingPreviewInfo);

        if (craftingPreviewInfo.hasMissing()) {
            return new CalculationResult(CalculationResultType.MISSING, previewElements, null);
        }

        return new CalculationResult(
            CalculationResultType.OK,
            previewElements,
            new CraftingTask(network, requested, quantity, pattern, nodes, toExtractInitial, toExtractInitialFluids)
        );
    }

    private void calculateInternal(
        int qty,
        IStackList<ItemStack> storageSource,
        IStackList<FluidStack> fluidStorageSource,
        IStackList<ItemStack> results,
        IStackList<FluidStack> fluidResults,
        ICraftingPattern pattern,
        boolean root) throws CraftingCalculatorException {

        if (System.currentTimeMillis() - calculationStarted > RS.SERVER_CONFIG.getAutocrafting().getCalculationTimeoutMs()) {
            throw new CraftingCalculatorException(CalculationResultType.TOO_COMPLEX, null);
        }

        if (!patternsUsed.add(pattern)) {
            throw new CraftingCalculatorException(CalculationResultType.RECURSIVE, pattern);
        }

        IStackList<ItemStack> itemsToExtract = API.instance().createItemStackList();
        IStackList<FluidStack> fluidsToExtract = API.instance().createFluidStackList();

        CraftingPatternInputs inputs = new CraftingPatternInputs(pattern);

        Node node = nodes.createOrAddToExistingNode(pattern, root, inputs, qty);

        calculateForItems(qty, storageSource, fluidStorageSource, results, fluidResults, itemsToExtract, inputs, node);

        if (node instanceof CraftingNode) {
            ItemStack output = pattern.getOutput(inputs.getRecipe());
            results.add(output, output.getCount() * qty);

            for (ItemStack byproduct : pattern.getByproducts(inputs.getRecipe())) {
                results.add(byproduct, byproduct.getCount() * qty);
            }
        } else if (node instanceof ProcessingNode) {
            ProcessingNode processing = (ProcessingNode) node;

            calculateForFluids(qty, storageSource, fluidStorageSource, results, fluidResults, pattern, inputs, fluidsToExtract, processing);

            for (ItemStack output : pattern.getOutputs()) {
                results.add(output, output.getCount() * qty);
            }

            for (FluidStack output : pattern.getFluidOutputs()) {
                fluidResults.add(output, output.getAmount() * qty);
            }
        }

        patternsUsed.remove(pattern);
    }

    private void calculateForItems(int qty,
                                   IStackList<ItemStack> storageSource,
                                   IStackList<FluidStack> fluidStorageSource,
                                   IStackList<ItemStack> results,
                                   IStackList<FluidStack> fluidResults,
                                   IStackList<ItemStack> itemsToExtract,
                                   CraftingPatternInputs inputs,
                                   Node node) throws CraftingCalculatorException {
        int ingredientNumber = -1;

        for (Ingredient<ItemStack> ingredient : inputs.getItemIngredients()) {
            ingredientNumber++;

            PossibleInputs<ItemStack> possibleInputs = new PossibleInputs<>(ingredient.getInputs());
            possibleInputs.sort(storageSource, results);

            ItemStack possibleInput = possibleInputs.get();

            ItemStack fromSelf = results.get(possibleInput);
            ItemStack fromNetwork = storageSource.get(possibleInput);

            int remaining = ingredient.getCount() * qty;

            if (remaining < 0) { // int overflow
                throw new CraftingCalculatorException(CalculationResultType.TOO_COMPLEX);
            }

            while (remaining > 0) {
                if (fromSelf != null) {
                    int toTake = Math.min(remaining, fromSelf.getCount());

                    node.getRequirements().addItemRequirement(ingredientNumber, possibleInput, toTake, ingredient.getCount());

                    results.remove(fromSelf, toTake);

                    remaining -= toTake;

                    fromSelf = results.get(possibleInput);
                }

                if (fromNetwork != null && remaining > 0) {
                    int toTake = Math.min(remaining, fromNetwork.getCount());

                    craftingPreviewInfo.getToTake().add(possibleInput, toTake);

                    node.getRequirements().addItemRequirement(ingredientNumber, possibleInput, toTake, ingredient.getCount());

                    storageSource.remove(fromNetwork, toTake);

                    remaining -= toTake;

                    fromNetwork = storageSource.get(possibleInput);

                    toExtractInitial.add(possibleInput, toTake);
                }

                if (remaining > 0) {
                    ICraftingPattern subPattern = network.getCraftingManager().getPattern(possibleInput);

                    if (subPattern != null) {
                        int qtyPerCraft = getQuantityPerCraft(possibleInput, null, subPattern);
                        int subQty = ((remaining - 1) / qtyPerCraft) + 1;

                        calculateInternal(subQty, storageSource, fluidStorageSource, results, fluidResults, subPattern, false);

                        fromSelf = results.get(possibleInput);
                        if (fromSelf == null) {
                            throw new IllegalStateException("Recursive calculation didn't yield anything");
                        }

                        fromNetwork = storageSource.get(possibleInput);

                        // fromSelf contains the amount crafted after the loop.
                        craftingPreviewInfo.getToCraft().add(fromSelf.copy());
                    } else {
                        if (!possibleInputs.cycle()) {
                            // Give up.
                            possibleInput = possibleInputs.get(); // Revert back to 0.

                            craftingPreviewInfo.getMissing().add(possibleInput, remaining);

                            itemsToExtract.add(possibleInput, remaining);

                            remaining = 0;
                        } else {
                            // Retry with new input...
                            possibleInput = possibleInputs.get();

                            fromSelf = results.get(possibleInput);
                            fromNetwork = storageSource.get(possibleInput);
                        }
                    }
                }
            }
        }
    }

    private void calculateForFluids(int qty,
                                    IStackList<ItemStack> storageSource,
                                    IStackList<FluidStack> fluidStorageSource,
                                    IStackList<ItemStack> results,
                                    IStackList<FluidStack> fluidResults,
                                    ICraftingPattern pattern,
                                    CraftingPatternInputs inputs,
                                    IStackList<FluidStack> fluidsToExtract,
                                    ProcessingNode node) throws CraftingCalculatorException {
        int ingredientNumber = -1;

        for (Ingredient<FluidStack> ingredient : inputs.getFluidIngredients()) {
            ingredientNumber++;

            PossibleInputs<FluidStack> possibleInputs = new PossibleInputs<>(ingredient.getInputs());
            possibleInputs.sort(fluidStorageSource, fluidResults);

            FluidStack possibleInput = possibleInputs.get();

            FluidStack fromSelf = fluidResults.get(possibleInput, IComparer.COMPARE_NBT);
            FluidStack fromNetwork = fluidStorageSource.get(possibleInput, IComparer.COMPARE_NBT);

            int remaining = possibleInput.getAmount() * qty;

            if (remaining < 0) { // int overflow
                throw new CraftingCalculatorException(CalculationResultType.TOO_COMPLEX);
            }

            while (remaining > 0) {
                if (fromSelf != null) {
                    int toTake = Math.min(remaining, fromSelf.getAmount());

                    node.getRequirements().addFluidRequirement(ingredientNumber, possibleInput, toTake, ingredient.getCount());

                    fluidResults.remove(possibleInput, toTake);

                    remaining -= toTake;

                    fromSelf = fluidResults.get(possibleInput, IComparer.COMPARE_NBT);
                }

                if (fromNetwork != null && remaining > 0) {
                    int toTake = Math.min(remaining, fromNetwork.getAmount());

                    node.getRequirements().addFluidRequirement(ingredientNumber, possibleInput, toTake, ingredient.getCount());

                    craftingPreviewInfo.getToTakeFluids().add(possibleInput, toTake);

                    fluidStorageSource.remove(fromNetwork, toTake);

                    remaining -= toTake;

                    fromNetwork = fluidStorageSource.get(possibleInput, IComparer.COMPARE_NBT);

                    toExtractInitialFluids.add(possibleInput, toTake);
                }

                if (remaining > 0) {
                    ICraftingPattern subPattern = network.getCraftingManager().getPattern(possibleInput);

                    if (subPattern != null) {
                        int qtyPerCraft = getQuantityPerCraft(null, possibleInput, subPattern);
                        int subQty = ((remaining - 1) / qtyPerCraft) + 1;

                        calculateInternal(subQty, storageSource, fluidStorageSource, results, fluidResults, subPattern, false);

                        fromSelf = fluidResults.get(possibleInput, IComparer.COMPARE_NBT);
                        if (fromSelf == null) {
                            throw new IllegalStateException("Recursive fluid calculation didn't yield anything");
                        }

                        fromNetwork = fluidStorageSource.get(possibleInput, IComparer.COMPARE_NBT);

                        // fromSelf contains the amount crafted after the loop.
                        craftingPreviewInfo.getToCraftFluids().add(fromSelf.copy());
                    } else {
                        if (!possibleInputs.cycle()) {
                            // Give up.
                            possibleInput = possibleInputs.get(); // Revert back to 0.

                            craftingPreviewInfo.getMissingFluids().add(possibleInput, remaining);

                            fluidsToExtract.add(possibleInput, remaining);

                            remaining = 0;
                        } else {
                            // Retry with new input...
                            possibleInput = possibleInputs.get();

                            fromSelf = fluidResults.get(possibleInput);
                            fromNetwork = fluidStorageSource.get(possibleInput);
                        }
                    }
                }
            }
        }
    }

    private int getQuantityPerCraft(@Nullable ItemStack item, @Nullable FluidStack fluid, ICraftingPattern pattern) {
        int qty = 0;

        if (item != null) {
            for (ItemStack output : pattern.getOutputs()) {
                if (API.instance().getComparer().isEqualNoQuantity(output, item)) {
                    qty += output.getCount();

                    if (!pattern.isProcessing()) {
                        break;
                    }
                }
            }
        } else if (fluid != null) {
            for (FluidStack output : pattern.getFluidOutputs()) {
                if (API.instance().getComparer().isEqual(output, fluid, IComparer.COMPARE_NBT)) {
                    qty += output.getAmount();
                }
            }
        }

        return qty;
    }
}
