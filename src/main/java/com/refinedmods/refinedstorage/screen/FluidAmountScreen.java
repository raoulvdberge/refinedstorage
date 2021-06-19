package com.refinedmods.refinedstorage.screen;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.container.FluidAmountContainer;
import com.refinedmods.refinedstorage.network.SetFluidFilterSlotMessage;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

public class FluidAmountScreen extends AmountSpecifyingScreen<FluidAmountContainer> {
    private final int containerSlot;
    private final FluidStack stack;
    private final int maxAmount;
    @Nullable
    private final UnaryOperator<Screen> alternativesScreenFactory;

    public FluidAmountScreen(BaseScreen parent, PlayerEntity player, int containerSlot, FluidStack stack, int maxAmount, @Nullable UnaryOperator<Screen> alternativesScreenFactory) {
        super(parent, new FluidAmountContainer(player, stack), alternativesScreenFactory != null ? 194 : 172, 99, player.inventory, new TranslationTextComponent("gui.refinedstorage.fluid_amount"));

        this.containerSlot = containerSlot;
        this.stack = stack;
        this.maxAmount = maxAmount;
        this.alternativesScreenFactory = alternativesScreenFactory;
    }

    @Override
    protected int getOkCancelButtonWidth() {
        return alternativesScreenFactory != null ? 75 : super.getOkCancelButtonWidth();
    }

    @Override
    public void onPostInit(int x, int y) {
        super.onPostInit(x, y);

        if (alternativesScreenFactory != null) {
            addButton(x + 114, cancelButton.y + 24, getOkCancelButtonWidth(), 20, new TranslationTextComponent("gui.refinedstorage.alternatives"), true, true, btn -> minecraft.displayGuiScreen(alternativesScreenFactory.apply(this)));
        }
    }

    @Override
    protected Pair<Integer, Integer> getOkCancelPos() {
        if (alternativesScreenFactory == null) {
            return super.getOkCancelPos();
        }

        return Pair.of(114, 22);
    }

    @Override
    protected int getDefaultAmount() {
        return stack.getAmount();
    }

    @Override
    protected boolean canAmountGoNegative() {
        return false;
    }

    @Override
    protected int getMaxAmount() {
        return maxAmount;
    }

    @Override
    protected ITextComponent getOkButtonText() {
        return new TranslationTextComponent("misc.refinedstorage.set");
    }

    @Override
    protected String getTexture() {
        return alternativesScreenFactory != null ? "gui/amount_specifying_wide.png" : "gui/amount_specifying.png";
    }

    @Override
    protected int[] getIncrements() {
        return new int[]{
            100, 500, 1000,
            -100, -500, -1000
        };
    }

    @Override
    protected void onValidAmountSaved(boolean shiftDown, int amount) {
        RS.NETWORK_HANDLER.sendToServer(new SetFluidFilterSlotMessage(containerSlot, StackUtils.copy(stack, amount)));
    }
}
