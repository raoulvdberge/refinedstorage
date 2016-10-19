package com.raoulvdberge.refinedstorage.gui;

import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.apiimpl.storage.fluid.FluidRenderer;
import com.raoulvdberge.refinedstorage.container.ContainerFluidInterface;
import com.raoulvdberge.refinedstorage.gui.sidebutton.SideButtonCompare;
import com.raoulvdberge.refinedstorage.gui.sidebutton.SideButtonRedstoneMode;
import com.raoulvdberge.refinedstorage.tile.TileFluidInterface;

public class GuiFluidInterface extends GuiBase {
    private static final FluidRenderer TANK_RENDERER = new FluidRenderer(TileFluidInterface.TANK_CAPACITY, 12, 47);

    public GuiFluidInterface(ContainerFluidInterface container) {
        super(container, 211, 204);
    }

    @Override
    public void init(int x, int y) {
        addSideButton(new SideButtonRedstoneMode(this, TileFluidInterface.REDSTONE_MODE));

        addSideButton(new SideButtonCompare(this, TileFluidInterface.COMPARE, IComparer.COMPARE_NBT));
    }

    @Override
    public void update(int x, int y) {
    }

    @Override
    public void drawBackground(int x, int y, int mouseX, int mouseY) {
        bindTexture("gui/fluid_interface.png");

        drawTexture(x, y, 0, 0, width, height);

        if (TileFluidInterface.TANK_IN.getValue() != null) {
            TANK_RENDERER.draw(mc, x + 46, y + 56, TileFluidInterface.TANK_IN.getValue());
        }

        if (TileFluidInterface.TANK_OUT.getValue() != null) {
            TANK_RENDERER.draw(mc, x + 118, y + 56, TileFluidInterface.TANK_OUT.getValue());
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        drawString(7, 7, t("gui.refinedstorage:fluid_interface"));
        drawString(43 + 4, 20, t("gui.refinedstorage:fluid_interface.in"));
        drawString(115 + 1, 20, t("gui.refinedstorage:fluid_interface.out"));
        drawString(7, 111, t("container.inventory"));

        if (inBounds(46, 56, 12, 47, mouseX, mouseY) && TileFluidInterface.TANK_IN.getValue() != null) {
            drawTooltip(mouseX, mouseY, TileFluidInterface.TANK_IN.getValue().getLocalizedName() + "\n" + TileFluidInterface.TANK_IN.getValue().amount + " mB");
        }

        if (inBounds(118, 56, 12, 47, mouseX, mouseY) && TileFluidInterface.TANK_OUT.getValue() != null) {
            drawTooltip(mouseX, mouseY, TileFluidInterface.TANK_OUT.getValue().getLocalizedName() + "\n" + TileFluidInterface.TANK_OUT.getValue().amount + " mB");
        }
    }
}
