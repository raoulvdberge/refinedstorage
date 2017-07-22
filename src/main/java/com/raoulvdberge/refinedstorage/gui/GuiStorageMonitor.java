package com.raoulvdberge.refinedstorage.gui;

import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.container.ContainerStorageMonitor;
import com.raoulvdberge.refinedstorage.gui.sidebutton.SideButtonCompare;
import com.raoulvdberge.refinedstorage.gui.sidebutton.SideButtonType;
import com.raoulvdberge.refinedstorage.integration.forestry.IntegrationForestry;
import com.raoulvdberge.refinedstorage.tile.TileStorageMonitor;

public class GuiStorageMonitor extends GuiBase {
    public GuiStorageMonitor(ContainerStorageMonitor container) {
        super(container, 211, 137);
    }

    @Override
    public void init(int x, int y) {
        addSideButton(new SideButtonType(this, TileStorageMonitor.TYPE));

        addSideButton(new SideButtonCompare(this, TileStorageMonitor.COMPARE, IComparer.COMPARE_DAMAGE));
        addSideButton(new SideButtonCompare(this, TileStorageMonitor.COMPARE, IComparer.COMPARE_NBT));
        addSideButton(new SideButtonCompare(this, TileStorageMonitor.COMPARE, IComparer.COMPARE_OREDICT));
		if(IntegrationForestry.isLoaded()) {
			addSideButton(new SideButtonCompare(this, TileStorageMonitor.COMPARE,
			IComparer.COMPARE_FORESTRY | IntegrationForestry.Tag.GEN.getFlag() | IntegrationForestry.Tag.IS_ANALYZED.getFlag()));
        }
    }

    @Override
    public void update(int x, int y) {
    }

    @Override
    public void drawBackground(int x, int y, int mouseX, int mouseY) {
        bindTexture("gui/storage_monitor.png");

        drawTexture(x, y, 0, 0, screenWidth, screenHeight);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        drawString(7, 7, t("gui.refinedstorage:storage_monitor"));
        drawString(7, 43, t("container.inventory"));
    }
}
