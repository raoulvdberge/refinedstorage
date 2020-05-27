package com.refinedmods.refinedstorage.apiimpl.network.grid.handler;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.tile.grid.portable.IPortableGrid;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PortableItemGridHandler implements IItemGridHandler {
    private IPortableGrid portableGrid;
    private IGrid grid;

    public PortableItemGridHandler(IPortableGrid portableGrid, IGrid grid) {
        this.portableGrid = portableGrid;
        this.grid = grid;
    }

    @Override
    public void onExtract(ServerPlayerEntity player, UUID id, int flags) {
        if (portableGrid.getStorage() == null || !grid.isGridActive()) {
            return;
        }

        ItemStack item = portableGrid.getItemCache().getList().get(id);

        if (item == null) {
            return;
        }

        int itemSize = item.getCount();
        // We copy here because some mods change the NBT tag of an item after getting the stack limit
        int maxItemSize = item.getItem().getItemStackLimit(item.copy());

        boolean single = (flags & EXTRACT_SINGLE) == EXTRACT_SINGLE;

        ItemStack held = player.inventory.getItemStack();

        if (single) {
            if (!held.isEmpty() && (!API.instance().getComparer().isEqualNoQuantity(item, held) || held.getCount() + 1 > held.getMaxStackSize())) {
                return;
            }
        } else if (!player.inventory.getItemStack().isEmpty()) {
            return;
        }

        int size = 64;

        if ((flags & EXTRACT_HALF) == EXTRACT_HALF && itemSize > 1) {
            size = itemSize / 2;

            // Rationale for this check:
            // If we have 32 buckets, and we want to extract half, we expect/need to get 8 (max stack size 16 / 2).
            // Without this check, we would get 16 (total stack size 32 / 2).
            // Max item size also can't be 1. Otherwise, if we want to extract half of 8 lava buckets, we would get size 0 (1 / 2).
            if (size > maxItemSize / 2 && maxItemSize != 1) {
                size = maxItemSize / 2;
            }
        } else if (single) {
            size = 1;
        } else if ((flags & EXTRACT_SHIFT) == EXTRACT_SHIFT) {
            // NO OP, the quantity already set (64) is needed for shift
        }

        size = Math.min(size, maxItemSize);

        // Do this before actually extracting, since portable grid sends updates as soon as a change happens (so before the storage tracker used to track)
        portableGrid.getItemStorageTracker().changed(player, item.copy());

        ItemStack took = portableGrid.getItemStorage().extract(item, size, IComparer.COMPARE_NBT, Action.SIMULATE);

        if (!took.isEmpty()) {
            if ((flags & EXTRACT_SHIFT) == EXTRACT_SHIFT) {
                IItemHandler playerInventory = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(null);

                if (playerInventory != null && ItemHandlerHelper.insertItem(playerInventory, took, true).isEmpty()) {
                    took = portableGrid.getItemStorage().extract(item, size, IComparer.COMPARE_NBT, Action.PERFORM);

                    ItemHandlerHelper.insertItem(playerInventory, took, false);
                }
            } else {
                took = portableGrid.getItemStorage().extract(item, size, IComparer.COMPARE_NBT, Action.PERFORM);

                if (single && !held.isEmpty()) {
                    held.grow(1);
                } else {
                    player.inventory.setItemStack(took);
                }

                player.updateHeldItem();
            }

            portableGrid.drainEnergy(RS.SERVER_CONFIG.getPortableGrid().getExtractUsage());
        }
    }

    @Override
    @Nonnull
    public ItemStack onInsert(ServerPlayerEntity player, ItemStack stack) {
        if (portableGrid.getStorage() == null || !grid.isGridActive()) {
            return stack;
        }

        portableGrid.getItemStorageTracker().changed(player, stack.copy());

        ItemStack remainder = portableGrid.getItemStorage().insert(stack, stack.getCount(), Action.PERFORM);

        portableGrid.drainEnergy(RS.SERVER_CONFIG.getPortableGrid().getInsertUsage());

        return remainder;
    }

    @Override
    public void onInsertHeldItem(ServerPlayerEntity player, boolean single) {
        if (player.inventory.getItemStack().isEmpty() || portableGrid.getStorage() == null || !grid.isGridActive()) {
            return;
        }

        ItemStack stack = player.inventory.getItemStack();
        int size = single ? 1 : stack.getCount();

        portableGrid.getItemStorageTracker().changed(player, stack.copy());

        if (single) {
            if (portableGrid.getItemStorage().insert(stack, size, Action.SIMULATE).isEmpty()) {
                portableGrid.getItemStorage().insert(stack, size, Action.PERFORM);

                stack.shrink(size);
            }
        } else {
            player.inventory.setItemStack(portableGrid.getItemStorage().insert(stack, size, Action.PERFORM));
        }

        player.updateHeldItem();

        portableGrid.drainEnergy(RS.SERVER_CONFIG.getPortableGrid().getInsertUsage());
    }

    @Override
    public void onCraftingPreviewRequested(ServerPlayerEntity player, UUID id, int quantity, boolean noPreview) {
        // NO OP
    }

    @Override
    public void onCraftingRequested(ServerPlayerEntity player, UUID id, int quantity) {
        // NO OP
    }

    @Override
    public void onCraftingCancelRequested(ServerPlayerEntity player, @Nullable UUID id) {
        // NO OP
    }
}
