package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingPatternInputs;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.IoUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;
import java.util.Queue;

public class CraftingNode extends Node {
    public CraftingNode(ICraftingPattern pattern, boolean root, CraftingPatternInputs inputs) {
        super(pattern, root, inputs);
    }

    public CraftingNode(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        super(network, tag);
    }

    @Override
    public void update(INetwork network, int ticks, NodeList nodes, IStorageDisk<ItemStack> internalStorage, IStorageDisk<FluidStack> internalFluidStorage, NodeListener listener) {
        for (ICraftingPatternContainer container : network.getCraftingManager().getAllContainers(getPattern())) {
            int interval = container.getUpdateInterval();
            if (interval < 0) {
                throw new IllegalStateException(container + " has an update interval of < 0");
            }

            if (interval == 0 || ticks % interval == 0) {
                for (int i = 0; i < container.getMaximumSuccessfulCraftingUpdates(); i++) {
                    if (IoUtil.extractFromInternalItemStorage(requirements.getSingleItemRequirementSet(true), internalStorage, Action.SIMULATE) != null) {
                        Map<Integer, Queue<ItemStack>> extracted = IoUtil.extractFromInternalItemStorage(requirements.getSingleItemRequirementSet(false), internalStorage, Action.PERFORM);

                        NonNullList<ItemStack> appliedRecipe = requirements.getItemsAsList(extracted, false);
                        ItemStack output = getPattern().getOutput(appliedRecipe);

                        if (!isRoot()) {
                            internalStorage.insert(output, output.getCount(), Action.PERFORM);
                        } else {
                            ItemStack remainder = network.insertItem(output, output.getCount(), Action.PERFORM);

                            internalStorage.insert(remainder, remainder.getCount(), Action.PERFORM);
                        }

                        // Byproducts need to always be inserted in the internal storage for later reuse further in the task.
                        // Regular outputs can be inserted into the network *IF* it's a root since it's *NOT* expected to be used later on.
                        for (ItemStack byp : getPattern().getByproducts(appliedRecipe)) {
                            internalStorage.insert(byp, byp.getCount(), Action.PERFORM);
                        }

                        next();

                        listener.onSingleDone(this);

                        if (getQuantity() <= 0) {
                            listener.onAllDone(this);
                            return;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }
}
