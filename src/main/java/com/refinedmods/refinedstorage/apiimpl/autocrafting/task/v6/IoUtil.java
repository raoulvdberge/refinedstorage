package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class IoUtil {
    private static final Logger LOGGER = LogManager.getLogger(IoUtil.class);

    private static final int DEFAULT_EXTRACT_FLAGS = IComparer.COMPARE_NBT;

    public static Map<Integer, Queue<ItemStack>> extractFromInternalItemStorage(Map<Integer, IStackList<ItemStack>> stacks, IStorageDisk<ItemStack> storage, Action action) {
        Map<Integer, Queue<ItemStack>> extracted = new HashMap<>();
        for (Map.Entry<Integer, IStackList<ItemStack>> entry : stacks.entrySet()) {
            Queue<ItemStack> queue = new ArrayDeque<>();
            for (StackListEntry<ItemStack> listEntry : entry.getValue().getStacks()) {
                ItemStack result = storage.extract(listEntry.getStack(), listEntry.getStack().getCount(), DEFAULT_EXTRACT_FLAGS, action);

                if (result == ItemStack.EMPTY || result.getCount() != listEntry.getStack().getCount()) {
                    if (action == Action.PERFORM) {
                        throw new IllegalStateException("The internal crafting inventory reported that " + listEntry.getStack() + " was available but we got " + result);
                    }

                    return null;
                }

                queue.add(result);
            }

            extracted.put(entry.getKey(), queue);
        }

        return extracted;
    }

    public static Map<Integer, Queue<FluidStack>> extractFromInternalFluidStorage(Map<Integer, IStackList<FluidStack>> stacks, IStorageDisk<FluidStack> storage, Action action) {
        Map<Integer, Queue<FluidStack>> extracted = new HashMap<>();
        for (Map.Entry<Integer, IStackList<FluidStack>> entry : stacks.entrySet()) {
            Queue<FluidStack> queue = new ArrayDeque<>();
            for (StackListEntry<FluidStack> listEntry : entry.getValue().getStacks()) {
                FluidStack result = storage.extract(listEntry.getStack(), listEntry.getStack().getAmount(), DEFAULT_EXTRACT_FLAGS, action);

                if (result.isEmpty() || result.getAmount() != listEntry.getStack().getAmount()) {
                    if (action == Action.PERFORM) {
                        throw new IllegalStateException("The internal crafting inventory reported that " + listEntry.getStack() + " was available but we got " + result);
                    }

                    return null;
                }

                queue.add(result);
            }

            extracted.put(entry.getKey(), queue);
        }

        return extracted;
    }

    public static boolean insertIntoInventory(@Nullable IItemHandler dest, NonNullList<ItemStack> toInsert, Action action) {
        if (toInsert.isEmpty()) {
            return true;
        }

        if (dest == null) {
            return false;
        }

        Deque<ItemStack> stacks = new ArrayDeque<>(toInsert);

        ItemStack current = stacks.poll();

        List<Integer> availableSlots = IntStream.range(0, dest.getSlots()).boxed().collect(Collectors.toList());

        while (current != null && !availableSlots.isEmpty()) {
            ItemStack remainder = ItemStack.EMPTY;

            for (int i = 0; i < availableSlots.size(); ++i) {
                int slot = availableSlots.get(i);

                // .copy() is mandatory!
                remainder = dest.insertItem(slot, current.copy(), action == Action.SIMULATE);

                // If we inserted *something*
                if (remainder.isEmpty() || current.getCount() != remainder.getCount()) {
                    availableSlots.remove(i);
                    break;
                }
            }

            if (remainder.isEmpty()) { // If we inserted successfully, get a next stack.
                current = stacks.poll();

            } else if (current.getCount() == remainder.getCount()) { // If we didn't insert anything over ALL these slots, stop here.
                break;
            } else { // If we didn't insert all, continue with other slots and use our remainder.
                current = remainder;
            }
        }

        boolean success = current == null && stacks.isEmpty();

        if (!success && action == Action.PERFORM) {
            LOGGER.warn("Inventory unexpectedly didn't accept " + (current != null ? current.getTranslationKey() : null) + ", the remainder has been voided!");
        }

        return success;
    }

    public static boolean insertIntoInventory(IFluidHandler dest, NonNullList<FluidStack> toInsert, Action action) {
        for (FluidStack stack : toInsert) {
            int filled = dest.fill(stack, action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

            if (filled != stack.getAmount()) {
                if (action == Action.PERFORM) {
                    LOGGER.warn("Inventory unexpectedly didn't accept all of " + stack.getTranslationKey() + ", the remainder has been voided!");
                }

                return false;
            }
        }

        return true;
    }

    public static void extractItemsFromNetwork(IStackList<ItemStack> toExtractInitial, INetwork network, IStorageDisk<ItemStack> internalStorage) {
        if (toExtractInitial.isEmpty()) {
            return;
        }

        List<ItemStack> toRemove = new ArrayList<>();

        for (StackListEntry<ItemStack> toExtract : toExtractInitial.getStacks()) {
            ItemStack result = network.extractItem(toExtract.getStack(), toExtract.getStack().getCount(), Action.PERFORM);

            if (!result.isEmpty()) {
                internalStorage.insert(toExtract.getStack(), result.getCount(), Action.PERFORM);

                toRemove.add(result);
            }
        }

        for (ItemStack stack : toRemove) {
            toExtractInitial.remove(stack);
        }

        if (!toRemove.isEmpty()) {
            network.getCraftingManager().onTaskChanged();
        }
    }

    public static void extractFluidsFromNetwork(IStackList<FluidStack> toExtractInitial, INetwork network, IStorageDisk<FluidStack> internalStorage) {
        if (toExtractInitial.isEmpty()) {
            return;
        }

        List<FluidStack> toRemove = new ArrayList<>();

        for (StackListEntry<FluidStack> toExtract : toExtractInitial.getStacks()) {
            FluidStack result = network.extractFluid(toExtract.getStack(), toExtract.getStack().getAmount(), Action.PERFORM);

            if (!result.isEmpty()) {
                internalStorage.insert(toExtract.getStack(), result.getAmount(), Action.PERFORM);

                toRemove.add(result);
            }
        }

        for (FluidStack stack : toRemove) {
            toExtractInitial.remove(stack);
        }

        if (!toRemove.isEmpty()) {
            network.getCraftingManager().onTaskChanged();
        }
    }
}
