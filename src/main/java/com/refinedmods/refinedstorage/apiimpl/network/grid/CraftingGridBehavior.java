package com.refinedmods.refinedstorage.apiimpl.network.grid;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.ICraftingGridBehavior;
import com.refinedmods.refinedstorage.api.network.grid.INetworkAwareGrid;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.item.PatternItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CraftingGridBehavior implements ICraftingGridBehavior {
    @Override
    public void onCrafted(INetworkAwareGrid grid, ICraftingRecipe recipe, PlayerEntity player, @Nullable IStackList<ItemStack> availableItems, @Nullable IStackList<ItemStack> usedItems) {
        NonNullList<ItemStack> remainder = recipe.getRemainingItems(grid.getCraftingMatrix());

        INetwork network = grid.getNetwork();

        CraftingInventory matrix = grid.getCraftingMatrix();

        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            ItemStack slot = matrix.getStackInSlot(i);

            // Do we have a remainder?
            if (i < remainder.size() && !remainder.get(i).isEmpty()) {
                // If there is no space for the remainder, dump it in the player inventory.
                if (!slot.isEmpty() && slot.getCount() > 1) {
                    if (!player.inventory.addItemStackToInventory(remainder.get(i).copy())) { // If there is no space in the player inventory, try to dump it in the network.
                        ItemStack remainderStack = network == null ? remainder.get(i).copy() : network.insertItem(remainder.get(i).copy(), remainder.get(i).getCount(), Action.PERFORM);

                        // If there is no space in the network, just dump it in the world.
                        if (!remainderStack.isEmpty()) {
                            InventoryHelper.spawnItemStack(player.getEntityWorld(), player.getPosX(), player.getPosY(), player.getPosZ(), remainderStack);
                        }
                    }

                    matrix.decrStackSize(i, 1);
                } else {
                    matrix.setInventorySlotContents(i, remainder.get(i).copy());
                }
            } else if (!slot.isEmpty()) { // We don't have a remainder, but the slot is not empty.
                if (slot.getCount() == 1 && network != null && grid.isGridActive()) { // Attempt to refill the slot with the same item from the network, only if we have a network and only if it's the last item.
                    ItemStack refill;
                    if (availableItems == null) { // for regular crafting
                        refill = network.extractItem(slot, 1, Action.PERFORM);
                    } else { // for shift crafting
                        if (availableItems.get(slot) != null) {
                            refill = availableItems.remove(slot, 1).getStack().copy();
                            refill.setCount(1);
                            usedItems.add(refill);
                        } else {
                            refill = ItemStack.EMPTY;
                        }
                    }

                    matrix.setInventorySlotContents(i, refill);

                    if (!refill.isEmpty()) {
                        network.getItemStorageTracker().changed(player, refill.copy());
                    }
                } else { // We don't have a network, or, the slot still has more than 1 items in it. Just decrement then.
                    matrix.decrStackSize(i, 1);
                }
            }
        }

        grid.onCraftingMatrixChanged();
    }

    @Override
    public void onCraftedShift(INetworkAwareGrid grid, PlayerEntity player) {
        CraftingInventory matrix = grid.getCraftingMatrix();
        INetwork network = grid.getNetwork();
        List<ItemStack> craftedItemsList = new ArrayList<>();
        ItemStack crafted = grid.getCraftingResult().getStackInSlot(0);

        int maxCrafted = crafted.getMaxStackSize();

        int amountCrafted = 0;
        boolean useNetwork = network != null && grid.isGridActive();

        IStackList<ItemStack> availableItems = API.instance().createItemStackList();
        if (useNetwork) {
            // We need a modifiable list of the items in storage that are relevant for this craft.
            // For performance reason we extract these into an extra list
            filterDuplicateStacks(network, matrix, availableItems);
        }

        //A second list to remember which items have been extracted
        IStackList<ItemStack> usedItems = API.instance().createItemStackList();

        ForgeHooks.setCraftingPlayer(player);
        // Do while the item is still craftable (aka is the result slot still the same as the original item?) and we don't exceed the max stack size.
        do {
            grid.onCrafted(player, availableItems, usedItems);

            craftedItemsList.add(crafted.copy());

            amountCrafted += crafted.getCount();
        } while (API.instance().getComparer().isEqual(crafted, grid.getCraftingResult().getStackInSlot(0)) && amountCrafted < maxCrafted && amountCrafted + crafted.getCount() <= maxCrafted);

        if (useNetwork) {
            usedItems.getStacks().forEach(stack -> network.extractItem(stack.getStack(), stack.getStack().getCount(), Action.PERFORM));
        }

        for (ItemStack craftedItem : craftedItemsList) {
            if (!player.inventory.addItemStackToInventory(craftedItem.copy())) {

                ItemStack remainder = craftedItem;

                if (useNetwork) {
                    remainder = network.insertItem(craftedItem, craftedItem.getCount(), Action.PERFORM);
                }

                if (!remainder.isEmpty()) {
                    InventoryHelper.spawnItemStack(player.getEntityWorld(), player.getPosX(), player.getPosY(), player.getPosZ(), remainder);
                }
            }
        }

        // @Volatile: This is some logic copied from CraftingResultSlot#onCrafting. We call this manually for shift clicking because
        // otherwise it's not being called.
        // For regular crafting, this is already called in ResultCraftingGridSlot#onTake -> onCrafting(stack)
        crafted.onCrafting(player.world, player, amountCrafted);
        BasicEventHooks.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), grid.getCraftingMatrix());
        ForgeHooks.setCraftingPlayer(null);
    }

    private void filterDuplicateStacks(INetwork network, CraftingInventory matrix, IStackList<ItemStack> availableItems) {
        for (int i = 0; i < matrix.getSizeInventory(); ++i) {
            ItemStack stack = network.getItemStorageCache().getList().get(matrix.getStackInSlot(i));

            //Don't add the same item twice into the list. Items may appear twice in a recipe but not in storage.
            if (stack != null && availableItems.get(stack) == null) {
                availableItems.add(stack);
            }
        }
    }

    @Override
    public void onRecipeTransfer(INetworkAwareGrid grid, PlayerEntity player, ItemStack[][] recipe) {
        INetwork network = grid.getNetwork();

        if (network != null && grid.getGridType() == GridType.CRAFTING && !network.getSecurityManager().hasPermission(Permission.EXTRACT, player)) {
            return;
        }

        // First try to empty the crafting matrix
        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            ItemStack slot = grid.getCraftingMatrix().getStackInSlot(i);

            if (!slot.isEmpty()) {
                // Only if we are a crafting grid. Pattern grids can just be emptied.
                if (grid.getGridType() == GridType.CRAFTING) {
                    // If we are connected, try to insert into network. If it fails, stop.
                    if (network != null && grid.isGridActive()) {
                        if (!network.insertItem(slot, slot.getCount(), Action.SIMULATE).isEmpty()) {
                            return;
                        } else {
                            network.insertItem(slot, slot.getCount(), Action.PERFORM);

                            network.getItemStorageTracker().changed(player, slot.copy());
                        }
                    } else {
                        // If we aren't connected, try to insert into player inventory. If it fails, stop.
                        if (!player.inventory.addItemStackToInventory(slot.copy())) {
                            return;
                        }
                    }
                }

                grid.getCraftingMatrix().setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }

        AtomicReference<Map<Item, ItemStack>> playerItems = new AtomicReference<>();
        // Now let's fill the matrix
        for (int i = 0; i < grid.getCraftingMatrix().getSizeInventory(); ++i) {
            if (recipe[i] != null) {
                ItemStack[] possibilities = recipe[i];

                if (network != null && grid.isGridActive() && network.getItemStorageCache() != null) {

                    // sort by the number of items in storage, craftables and inventory
                    Arrays.sort(possibilities, compareByItemStackCounts(player, network, playerItems));
                }

                // If we are a crafting grid
                if (grid.getGridType() == GridType.CRAFTING) {
                    boolean found = false;

                    // If we are connected, first try to get the possibilities from the network
                    if (network != null && grid.isGridActive()) {
                        for (ItemStack possibility : possibilities) {
                            ItemStack took = network.extractItem(possibility, 1, IComparer.COMPARE_NBT, Action.PERFORM);

                            if (!took.isEmpty()) {
                                grid.getCraftingMatrix().setInventorySlotContents(i, took);

                                network.getItemStorageTracker().changed(player, took.copy());

                                found = true;

                                break;
                            }
                        }
                    }

                    // If we haven't found anything in the network (or we are disconnected), go look in the player inventory
                    if (!found) {
                        for (ItemStack possibility : possibilities) {
                            for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
                                if (API.instance().getComparer().isEqual(possibility, player.inventory.getStackInSlot(j), IComparer.COMPARE_NBT)) {
                                    grid.getCraftingMatrix().setInventorySlotContents(i, ItemHandlerHelper.copyStackWithSize(player.inventory.getStackInSlot(j), 1));

                                    player.inventory.decrStackSize(j, 1);

                                    found = true;

                                    break;
                                }
                            }

                            if (found) {
                                break;
                            }
                        }
                    }
                } else if (grid.getGridType() == GridType.PATTERN) {
                    // If we are a pattern grid we can just set the slot
                    grid.getCraftingMatrix().setInventorySlotContents(i, possibilities.length == 0 ? ItemStack.EMPTY : possibilities[0]);
                }
            }
        }

        if (grid.getGridType() == GridType.PATTERN) {
            ((GridNetworkNode) grid).setProcessingPattern(false);
            ((GridNetworkNode) grid).markDirty();
        }
    }

    private Comparator<ItemStack> compareByItemStackCounts(PlayerEntity player, INetwork network, AtomicReference<Map<Item, ItemStack>> playerItems) {
        return Comparator.comparingInt((ItemStack itemStack) -> {

            ItemStack stack = network.getItemStorageCache().getList().get(itemStack);
            if (stack != null) {
                return stack.getCount();
            }

            if (network.getCraftingManager().getPattern(itemStack) != null) {
                return 1;
            }

            if (playerItems.get() == null) {
                playerItems.set(makePlayerInventoryMap(player, network));
            }

            ItemStack onPlayer = playerItems.get().get(itemStack.getItem());
            if (onPlayer != null) {
                return onPlayer.getCount();
            }
            return 0;
        }).reversed();
    }

    private Map<Item, ItemStack> makePlayerInventoryMap(PlayerEntity player, INetwork network) {
        Map<Item, ItemStack> playerItems = new HashMap<>();
        for (int j = 0; j < player.inventory.getSizeInventory(); j++) {
            ItemStack inventoryStack = player.inventory.getStackInSlot(j);

            if (inventoryStack.getItem() instanceof PatternItem) {
                ICraftingPattern pattern = PatternItem.fromCache(network.getWorld(), inventoryStack);
                if (pattern.isValid()) {
                    for (ItemStack stack : pattern.getOutputs()) {
                        if (!stack.isEmpty()) {
                            playerItems.put(stack.getItem(), stack);
                        }
                    }
                }
            } else {
                playerItems.put(inventoryStack.getItem(), inventoryStack);
            }
        }

        return playerItems;
    }

}
