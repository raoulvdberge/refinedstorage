package com.refinedmods.refinedstorage.container.slot.filter;

import com.refinedmods.refinedstorage.container.slot.BaseSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class FilterSlot extends BaseSlot {
    public static final int FILTER_ALLOW_SIZE = 1;
    public static final int FILTER_ALLOW_BLOCKS = 2;
    public static final int FILTER_ALLOW_ALTERNATIVES = 4;

    private int flags;

    public FilterSlot(IItemHandler handler, int inventoryIndex, int x, int y, int flags) {
        super(handler, inventoryIndex, x, y);

        this.flags = flags;
    }

    public FilterSlot(IItemHandler handler, int inventoryIndex, int x, int y) {
        this(handler, inventoryIndex, x, y, 0);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (super.isItemValid(stack)) {
            if (isBlockAllowed()) {
                return stack.getItem() instanceof BlockItem;
            }

            return true;
        }

        return false;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (!stack.isEmpty() && !isSizeAllowed()) {
            stack.setCount(1);
        }

        super.putStack(stack);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return false;
    }

    public boolean isSizeAllowed() {
        return (flags & FILTER_ALLOW_SIZE) == FILTER_ALLOW_SIZE;
    }

    public boolean isBlockAllowed() {
        return (flags & FILTER_ALLOW_BLOCKS) == FILTER_ALLOW_BLOCKS;
    }

    public boolean isAlternativesAllowed() {
        return (flags & FILTER_ALLOW_ALTERNATIVES) == FILTER_ALLOW_ALTERNATIVES;
    }
}
