package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.CraftingTaskReadException;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingPatternInputs;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.SerializationUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public abstract class Node {
    private static final String NBT_PATTERN = "Pattern";
    private static final String NBT_ROOT = "Root";
    private static final String NBT_IS_PROCESSING = "IsProcessing";
    private static final String NBT_QUANTITY = "Quantity";
    private static final String NBT_QUANTITY_TOTAL = "TotalQuantity";

    private final boolean root;
    private final ICraftingPattern pattern;

    protected int quantity;
    protected int totalQuantity;

    protected final NodeRequirements requirements;

    public Node(ICraftingPattern pattern, boolean root, CraftingPatternInputs inputs) {
        this.pattern = pattern;
        this.root = root;
        this.requirements = new NodeRequirements(inputs);
    }

    public Node(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        this.quantity = tag.getInt(NBT_QUANTITY);
        this.totalQuantity = tag.getInt(NBT_QUANTITY_TOTAL);
        this.pattern = SerializationUtil.readPatternFromNbt(tag.getCompound(NBT_PATTERN), network.getWorld());
        this.root = tag.getBoolean(NBT_ROOT);
        this.requirements = new NodeRequirements(tag);
    }

    public static Node fromNbt(INetwork network, CompoundNBT tag) throws CraftingTaskReadException {
        return tag.getBoolean(NBT_IS_PROCESSING) ? new ProcessingNode(network, tag) : new CraftingNode(network, tag);
    }

    public abstract void update(INetwork network, int ticks, NodeList nodes, IStorageDisk<ItemStack> internalStorage, IStorageDisk<FluidStack> internalFluidStorage, NodeListener listener);

    public void onCalculationFinished() {
        requirements.readMaxSlots();
        this.totalQuantity = quantity;
    }

    public ICraftingPattern getPattern() {
        return pattern;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    protected void next() {
        quantity--;
    }

    public boolean isRoot() {
        return root;
    }

    public NodeRequirements getRequirements() {
        return requirements;
    }

    public CompoundNBT writeToNbt() {
        CompoundNBT tag = new CompoundNBT();

        tag.putInt(NBT_QUANTITY, quantity);
        tag.putInt(NBT_QUANTITY_TOTAL, totalQuantity);
        tag.putBoolean(NBT_IS_PROCESSING, this instanceof ProcessingNode);
        tag.putBoolean(NBT_ROOT, root);
        tag.put(NBT_PATTERN, SerializationUtil.writePatternToNbt(pattern));
        tag = requirements.writeToNbt(tag);

        return tag;
    }
}
