package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.IoUtil;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.SerializationUtil;
import com.refinedmods.refinedstorage.apiimpl.util.FluidStackList;
import com.refinedmods.refinedstorage.apiimpl.util.ItemStackList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ProcessingNode extends Node {
    private static final String NBT_ITEMS_RECEIVED = "ItemsReceived";
    private static final String NBT_FLUIDS_RECEIVED = "FluidsReceived";
    private static final String NBT_SINGLE_ITEM_SET_TO_REQUIRE = "SingleItemSetToRequire";
    private static final String NBT_SINGLE_FLUID_SET_TO_REQUIRE = "SingleFluidSetToRequire";
    private static final String NBT_STATE = "State";

    private final IStackList<ItemStack> singleItemSetToReceive = API.instance().createItemStackList();
    private final IStackList<FluidStack> singleFluidSetToReceive = API.instance().createFluidStackList();

    private IStackList<ItemStack> singleItemSetToRequire;
    private IStackList<FluidStack> singleFluidSetToRequire;

    private IStackList<ItemStack> itemsReceived = API.instance().createItemStackList();
    private IStackList<FluidStack> fluidsReceived = API.instance().createFluidStackList();

    private ProcessingState state = ProcessingState.READY;

    private int quantityFinished;

    public ProcessingNode(ICraftingPattern pattern, boolean root) {
        super(pattern, root);

        initSetsToReceive();
    }

    public ProcessingNode(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        super(network, tag);

        this.itemsReceived = SerializationUtil.readItemStackList(tag.getList(NBT_ITEMS_RECEIVED, Constants.NBT.TAG_COMPOUND));
        this.fluidsReceived = SerializationUtil.readFluidStackList(tag.getList(NBT_FLUIDS_RECEIVED, Constants.NBT.TAG_COMPOUND));

        this.singleItemSetToRequire = SerializationUtil.readItemStackList(tag.getList(NBT_SINGLE_ITEM_SET_TO_REQUIRE, Constants.NBT.TAG_COMPOUND));
        this.singleFluidSetToRequire = SerializationUtil.readFluidStackList(tag.getList(NBT_SINGLE_FLUID_SET_TO_REQUIRE, Constants.NBT.TAG_COMPOUND));

        this.state = ProcessingState.values()[tag.getInt(NBT_STATE)];

        initSetsToReceive();
    }

    private void initSetsToReceive() {
        for (ItemStack output : getPattern().getOutputs()) {
            singleItemSetToReceive.add(output, output.getCount());
        }

        for (FluidStack output : getPattern().getFluidOutputs()) {
            singleFluidSetToReceive.add(output, output.getAmount());
        }
    }

    @Override
    public void update(INetwork network, int ticks, NodeList nodes, IStorageDisk<ItemStack> internalStorage, IStorageDisk<FluidStack> internalFluidStorage, NodeListener listener) {
        if (getQuantity() <= 0) {
            if (state == ProcessingState.PROCESSED) {
                listener.onAllDone(this);
            }
            return;
        }

        boolean allLocked = true;
        boolean allMissingMachine = true;
        boolean allRejected = true;

        ProcessingState originalState = state;

        for (ICraftingPatternContainer container : network.getCraftingManager().getAllContainers(getPattern())) {
            int interval = container.getUpdateInterval();

            if (interval < 0) {
                throw new IllegalStateException(container + " has an update interval of < 0");
            }

            if (interval == 0 || ticks % interval == 0) {
                for (int i = 0; i < container.getMaximumSuccessfulCraftingUpdates(); i++) {
                    if (getQuantity() <= 0) {
                        return;
                    }

                    if (container.isLocked()) {
                        if (allLocked) {
                            this.state = ProcessingState.LOCKED;
                        }

                        break;
                    } else {
                        allLocked = false;
                    }

                    if ((!singleItemSetToRequire.isEmpty() && container.getConnectedInventory() == null) ||
                        (!singleFluidSetToRequire.isEmpty() && container.getConnectedFluidInventory() == null)) {
                        if (allMissingMachine) {
                            this.state = ProcessingState.MACHINE_NONE;
                        }

                        break;
                    } else {
                        allMissingMachine = false;
                    }

                    boolean hasAllRequirements = false;

                    List<ItemStack> extractedItems = IoUtil.extractFromInternalItemStorage(requirements.getSingleItemRequirementSet(true), internalStorage, Action.SIMULATE);
                    List<FluidStack> extractedFluids = null;
                    if (extractedItems != null) {
                        extractedFluids = IoUtil.extractFromInternalFluidStorage(requirements.getSingleFluidRequirementSet(true), internalFluidStorage, Action.SIMULATE);
                        if (extractedFluids != null) {
                            hasAllRequirements = true;
                        }
                    }

                    boolean canInsertFullAmount = false;
                    if (hasAllRequirements) {
                        canInsertFullAmount = IoUtil.insertIntoInventory(container.getConnectedInventory(), extractedItems, Action.SIMULATE);
                        if (canInsertFullAmount) {
                            canInsertFullAmount = IoUtil.insertIntoInventory(container.getConnectedFluidInventory(), extractedFluids, Action.SIMULATE);
                        }
                    } else {
                        break;
                    }

                    if (!canInsertFullAmount) {
                        if (allRejected) {
                            this.state = ProcessingState.MACHINE_DOES_NOT_ACCEPT;
                        }

                        break;
                    } else {
                        allRejected = false;
                    }

                    this.state = ProcessingState.READY;

                    extractedItems = IoUtil.extractFromInternalItemStorage(requirements.getSingleItemRequirementSet(false), internalStorage, Action.PERFORM);
                    extractedFluids = IoUtil.extractFromInternalFluidStorage(requirements.getSingleFluidRequirementSet(false), internalFluidStorage, Action.PERFORM);

                    IoUtil.insertIntoInventory(container.getConnectedInventory(), extractedItems, Action.PERFORM);
                    IoUtil.insertIntoInventory(container.getConnectedFluidInventory(), extractedFluids, Action.PERFORM);

                    next();

                    listener.onSingleDone(this);

                    container.onUsedForProcessing();
                }
            }
        }

        if (originalState != state) {
            network.getCraftingManager().onTaskChanged();
        }
    }

    public ProcessingState getState() {
        return state;
    }

    public IStackList<ItemStack> getSingleItemSetToReceive() {
        return singleItemSetToReceive;
    }

    public IStackList<FluidStack> getSingleFluidSetToReceive() {
        return singleFluidSetToReceive;
    }

    public IStackList<ItemStack> getSingleItemSetToRequire() {
        return singleItemSetToRequire;
    }

    public IStackList<FluidStack> getSingleFluidSetToRequire() {
        return singleFluidSetToRequire;
    }

    public int getNeeded(ItemStack stack) {
        return (singleItemSetToReceive.getCount(stack) * totalQuantity) - itemsReceived.getCount(stack);
    }

    public int getNeeded(FluidStack stack) {
        return (singleFluidSetToReceive.getCount(stack) * totalQuantity) - fluidsReceived.getCount(stack);
    }

    public int getCurrentlyProcessing() {
        int unprocessed = totalQuantity - quantity;
        return unprocessed - quantityFinished;
    }

    public void markReceived(ItemStack stack, int count) {
        itemsReceived.add(stack, count);
        updateFinishedQuantity();
    }

    public void markReceived(FluidStack stack, int count) {
        fluidsReceived.add(stack, count);
        updateFinishedQuantity();
    }

    public void updateFinishedQuantity() {
        int tempQuantityFinished = totalQuantity;

        for (StackListEntry<ItemStack> toReceive : singleItemSetToReceive.getStacks()) {
            if (itemsReceived.get(toReceive.getStack()) != null) {
                int ratioReceived = itemsReceived.get(toReceive.getStack()).getCount() / toReceive.getStack().getCount();

                if (tempQuantityFinished > ratioReceived) {
                    tempQuantityFinished = ratioReceived;
                }
            } else {
                tempQuantityFinished = 0;
            }
        }

        for (StackListEntry<FluidStack> toReceive : singleFluidSetToReceive.getStacks()) {
            if (fluidsReceived.get(toReceive.getStack()) != null) {
                int ratioReceived = fluidsReceived.get(toReceive.getStack()).getAmount() / toReceive.getStack().getAmount();

                if (tempQuantityFinished > ratioReceived) {
                    tempQuantityFinished = ratioReceived;
                }
            } else {
                tempQuantityFinished = 0;
            }
        }

        this.quantityFinished = tempQuantityFinished;

        if (this.quantityFinished == this.totalQuantity) {
            this.state = ProcessingState.PROCESSED;
        }
    }

    @Override
    public void onCalculationFinished() {
        super.onCalculationFinished();

        this.singleItemSetToRequire = new ItemStackList(requirements.getSingleItemRequirementSet(true));
        this.singleFluidSetToRequire = new FluidStackList(requirements.getSingleFluidRequirementSet(true));
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT tag = super.writeToNbt();

        tag.put(NBT_ITEMS_RECEIVED, SerializationUtil.writeItemStackList(itemsReceived));
        tag.put(NBT_FLUIDS_RECEIVED, SerializationUtil.writeFluidStackList(fluidsReceived));

        tag.put(NBT_SINGLE_ITEM_SET_TO_REQUIRE, SerializationUtil.writeItemStackList(singleItemSetToRequire));
        tag.put(NBT_SINGLE_FLUID_SET_TO_REQUIRE, SerializationUtil.writeFluidStackList(singleFluidSetToRequire));

        tag.putInt(NBT_STATE, state.ordinal());

        return tag;
    }
}
