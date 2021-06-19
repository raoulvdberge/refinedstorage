package com.refinedmods.refinedstorage.screen;

import com.refinedmods.refinedstorage.tile.data.TileDataManager;
import com.refinedmods.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Pair;

public class PriorityScreen extends AmountSpecifyingScreen<Container> {
    private final TileDataParameter<Integer, ?> priority;

    public PriorityScreen(BaseScreen parent, TileDataParameter<Integer, ?> priority, PlayerInventory inventory) {
        super(parent, new Container(null, 0) {
            @Override
            public boolean canInteractWith(PlayerEntity player) {
                return false;
            }
        }, 164, 92, inventory, new TranslationTextComponent("misc.refinedstorage.priority"));

        this.priority = priority;
    }

    @Override
    protected int getDefaultAmount() {
        return priority.getValue();
    }

    @Override
    protected ITextComponent getOkButtonText() {
        return new TranslationTextComponent("misc.refinedstorage.set");
    }

    @Override
    protected String getTexture() {
        return "gui/priority.png";
    }

    @Override
    protected Pair<Integer, Integer> getAmountPos() {
        return Pair.of(18 + 1, 47 + 1);
    }

    @Override
    protected Pair<Integer, Integer> getOkCancelPos() {
        return Pair.of(107, 30);
    }

    @Override
    protected boolean canAmountGoNegative() {
        return true;
    }

    @Override
    protected int getMaxAmount() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int[] getIncrements() {
        return new int[]{
            1, 5, 10,
            -1, -5, -10
        };
    }

    @Override
    protected void onValidAmountSaved(boolean shiftDown, int amount) {
        TileDataManager.setParameter(priority, amount);
    }
}
