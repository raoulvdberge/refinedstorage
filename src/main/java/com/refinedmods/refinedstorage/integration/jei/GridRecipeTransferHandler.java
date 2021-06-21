package com.refinedmods.refinedstorage.integration.jei;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.network.grid.GridCraftingPreviewRequestMessage;
import com.refinedmods.refinedstorage.network.grid.GridProcessingTransferMessage;
import com.refinedmods.refinedstorage.network.grid.GridTransferMessage;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.grid.stack.IGridStack;
import com.refinedmods.refinedstorage.screen.grid.stack.ItemGridStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GridRecipeTransferHandler implements IRecipeTransferHandler<GridContainer> {
    public static final GridRecipeTransferHandler INSTANCE = new GridRecipeTransferHandler();

    private static final long TRANSFER_SCROLLBAR_DELAY_MS = 200;

    private long lastTransferTimeMs;

    private GridRecipeTransferHandler() {
    }

    @Override
    public Class<GridContainer> getContainerClass() {
        return GridContainer.class;
    }

    @Override
    public IRecipeTransferError transferRecipe(@Nonnull GridContainer container, Object recipe, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (!(container.getScreenInfoProvider() instanceof GridScreen)) {
            return null;
        }

        GridType type = container.getGrid().getGridType();

        if (type == GridType.CRAFTING) {
            return transferRecipeForCraftingGrid(container, recipeLayout, player, doTransfer);
        } else if (type == GridType.PATTERN) {
            return transferRecipeForPatternGrid(container, recipeLayout, player, doTransfer);
        }

        return null;
    }

    private RecipeTransferCraftingGridError transferRecipeForCraftingGrid(GridContainer container, IRecipeLayout recipeLayout, PlayerEntity player, boolean doTransfer) {
        IngredientTracker tracker = createTracker(container, recipeLayout, player, doTransfer);

        if (doTransfer) {
            if (tracker.hasMissingButAutocraftingAvailable() && Screen.hasControlDown()) {
                tracker.createCraftingRequests().forEach((id, count) -> RS.NETWORK_HANDLER.sendToServer(
                    new GridCraftingPreviewRequestMessage(
                        id,
                        count,
                        Screen.hasShiftDown(),
                        false
                    )
                ));
            } else {
                moveItems(container, recipeLayout, tracker);
            }
        } else {
            if (tracker.hasMissing()) {
                return new RecipeTransferCraftingGridError(tracker);
            }
        }

        return null;
    }

    private IRecipeTransferError transferRecipeForPatternGrid(GridContainer container, IRecipeLayout recipeLayout, PlayerEntity player, boolean doTransfer) {
        IngredientTracker tracker = createTracker(container, recipeLayout, player, doTransfer);

        if (doTransfer) {
            moveItems(container, recipeLayout, tracker);
        } else {
            if (tracker.isAutocraftingAvailable()) {
                return new RecipeTransferPatternGridError(tracker);
            }
        }

        return null;
    }

    private IngredientTracker createTracker(GridContainer container, IRecipeLayout recipeLayout, PlayerEntity player, boolean doTransfer) {
        IngredientTracker tracker = new IngredientTracker(recipeLayout, doTransfer);

        // Using IGridView#getStacks will return a *filtered* list of items in the view,
        // which will cause problems - especially if the user uses JEI synchronised searching.
        // Instead, we will use IGridView#getAllStacks which provides an unordered view of all GridStacks.
        Collection<IGridStack> gridStacks = ((GridScreen) container.getScreenInfoProvider()).getView().getAllStacks();

        // Check grid
        if (container.getGrid().isGridActive()) {
            for (IGridStack gridStack : gridStacks) {
                if (gridStack instanceof ItemGridStack) {
                    tracker.addAvailableStack(((ItemGridStack) gridStack).getStack(), gridStack);
                }
            }
        }

        // Check inventory
        for (int inventorySlot = 0; inventorySlot < player.inventory.getSizeInventory(); inventorySlot++) {
            if (!player.inventory.getStackInSlot(inventorySlot).isEmpty()) {
                tracker.addAvailableStack(player.inventory.getStackInSlot(inventorySlot), null);
            }
        }

        // Check grid crafting slots
        if (container.getGrid().getGridType().equals(GridType.CRAFTING)) {
            CraftingInventory craftingMatrix = container.getGrid().getCraftingMatrix();
            if (craftingMatrix != null) {
                for (int matrixSlot = 0; matrixSlot < craftingMatrix.getSizeInventory(); matrixSlot++) {
                    if (!craftingMatrix.getStackInSlot(matrixSlot).isEmpty()) {
                        tracker.addAvailableStack(craftingMatrix.getStackInSlot(matrixSlot), null);
                    }
                }
            }
        }

        return tracker;
    }

    public boolean hasTransferredRecently() {
        return System.currentTimeMillis() - lastTransferTimeMs <= TRANSFER_SCROLLBAR_DELAY_MS;
    }

    private void moveItems(GridContainer gridContainer, IRecipeLayout recipeLayout, IngredientTracker tracker) {
        this.lastTransferTimeMs = System.currentTimeMillis();

        if (gridContainer.getGrid().getGridType() == GridType.PATTERN && !recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
            moveForProcessing(recipeLayout, tracker);
        } else {
            move(gridContainer, recipeLayout);
        }
    }

    private void move(GridContainer gridContainer, IRecipeLayout recipeLayout) {
        RS.NETWORK_HANDLER.sendToServer(new GridTransferMessage(
            recipeLayout.getItemStacks().getGuiIngredients(),
            gridContainer.inventorySlots.stream().filter(s -> s.inventory instanceof CraftingInventory).collect(Collectors.toList())
        ));
    }

    private void moveForProcessing(IRecipeLayout recipeLayout, IngredientTracker tracker) {
        List<ItemStack> inputs = new LinkedList<>();
        List<ItemStack> outputs = new LinkedList<>();

        List<FluidStack> fluidInputs = new LinkedList<>();
        List<FluidStack> fluidOutputs = new LinkedList<>();

        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            handleItemIngredient(inputs, outputs, guiIngredient, tracker);
        }

        for (IGuiIngredient<FluidStack> guiIngredient : recipeLayout.getFluidStacks().getGuiIngredients().values()) {
            handleFluidIngredient(fluidInputs, fluidOutputs, guiIngredient);
        }

        RS.NETWORK_HANDLER.sendToServer(new GridProcessingTransferMessage(inputs, outputs, fluidInputs, fluidOutputs));
    }

    private void handleFluidIngredient(List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs, IGuiIngredient<FluidStack> guiIngredient) {
        if (guiIngredient != null && guiIngredient.getDisplayedIngredient() != null) {
            FluidStack ingredient = guiIngredient.getDisplayedIngredient().copy();

            if (guiIngredient.isInput()) {
                fluidInputs.add(ingredient);
            } else {
                fluidOutputs.add(ingredient);
            }
        }
    }

    private void handleItemIngredient(List<ItemStack> inputs, List<ItemStack> outputs, IGuiIngredient<ItemStack> guiIngredient, IngredientTracker tracker) {
        if (guiIngredient != null && guiIngredient.getDisplayedIngredient() != null) {
            ItemStack ingredient = tracker.findBestMatch(guiIngredient.getAllIngredients()).copy();

            if (ingredient == ItemStack.EMPTY) {
                ingredient = guiIngredient.getDisplayedIngredient().copy();
            }

            if (guiIngredient.isInput()) {
                inputs.add(ingredient);
            } else {
                outputs.add(ingredient);
            }
        }
    }
}
