package com.raoulvdberge.refinedstorage.api.autocrafting.task;

import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.util.IFluidStackList;
import com.raoulvdberge.refinedstorage.api.util.IItemStackList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.Deque;
import java.util.List;

/**
 * Represents a step in a crafting task that.
 */
public interface ICraftingStep {
    /**
     * @return the pattern
     */
    ICraftingPattern getPattern();

    /**
     * @return the stacks to insert, no null entries
     */
    List<ItemStack> getToInsert();

    /**
     * Check if the processing can start.
     *
     * @param items a list to compare the needed {@link ItemStack} inputs against
     * @param fluids a list to compare the needed {@link net.minecraftforge.fluids.FluidStack} inputs against (eg. a bucket, machine insert)
     * @return true if processing can start
     */
    boolean canStartProcessing(IItemStackList items, IFluidStackList fluids);

    /**
     * When called, this step will be marked as started processing.
     */
    void setStartedProcessing();

    /**
     * @return whether this step has started processing
     */
    boolean hasStartedProcessing();

    /**
     * Execute this step.
     * Any items to be added to the network should be inserting into these queues and they'll be managed by the {@link ICraftingTask}.
     *
     * @param toInsertItems a queue of items to be inserted into the network
     * @param toInsertFluids a queue of fluids to be inserted into the network
     */
    void execute(Deque<ItemStack> toInsertItems, Deque<FluidStack> toInsertFluids);

    /**
     * @return true if we received all outputs, false otherwise
     */
    boolean hasReceivedOutputs();

    /**
     * @param stack the output to check,
     * @return true if we received the given output (based upon item and stacksize), false otherwise
     */
    boolean hasReceivedOutput(ItemStack stack);

    /**
     * @param stack the stack that was inserted in the storage system
     * @return true if this item belonged to the processable item, false otherwise
     */
    boolean onReceiveOutput(ItemStack stack);

    /**
     * Writes the processable to NBT.
     *
     * @param tag the tag
     * @return the written tag
     */
    NBTTagCompound writeToNBT(NBTTagCompound tag);
}
