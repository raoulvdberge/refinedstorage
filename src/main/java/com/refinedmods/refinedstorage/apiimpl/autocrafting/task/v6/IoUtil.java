package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class IoUtil {
    private static final int DEFAULT_EXTRACT_FLAGS = IComparer.COMPARE_NBT;

    private IoUtil() {
    }

    public static IStackList<ItemStack> extractFromInternalItemStorage(IStackList<ItemStack> list, IStorageDisk<ItemStack> storage, Action action) {
        IStackList<ItemStack> extracted = API.instance().createItemStackList();

        for (StackListEntry<ItemStack> entry : list.getStacks()) {
            ItemStack result = storage.extract(entry.getStack(), entry.getStack().getCount(), DEFAULT_EXTRACT_FLAGS, action);

            if (result.isEmpty() || result.getCount() != entry.getStack().getCount()) {
                if (action == Action.PERFORM) {
                    throw new IllegalStateException("The internal crafting inventory reported that " + entry.getStack() + " was available but we got " + result);
                }

                return null;
            }

            extracted.add(result);
        }

        return extracted;
    }

    public static IStackList<FluidStack> extractFromInternalFluidStorage(IStackList<FluidStack> list, IStorageDisk<FluidStack> storage, Action action) {
        IStackList<FluidStack> extracted = API.instance().createFluidStackList();

        for (StackListEntry<FluidStack> entry : list.getStacks()) {
            FluidStack result = storage.extract(entry.getStack(), entry.getStack().getAmount(), DEFAULT_EXTRACT_FLAGS, action);

            if (result.isEmpty() || result.getAmount() != entry.getStack().getAmount()) {
                if (action == Action.PERFORM) {
                    throw new IllegalStateException("The internal crafting inventory reported that " + entry.getStack() + " was available but we got " + result);
                }

                return null;
            }

            extracted.add(result);
        }

        return extracted;
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
                internalStorage.insert(result, result.getAmount(), Action.PERFORM);

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
