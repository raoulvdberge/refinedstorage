package com.refinedmods.refinedstorage.screen.widget.sidebutton;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.integration.jei.JeiIntegration;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class GridSearchBoxModeSideButton extends SideButton {
    public GridSearchBoxModeSideButton(GridScreen screen) {
        super(screen);
    }

    @Override
    public String getTooltip() {
        return I18n.format("sidebutton.refinedstorage.grid.search_box_mode") + "\n" + TextFormatting.GRAY + I18n.format("sidebutton.refinedstorage.grid.search_box_mode." + ((GridScreen) screen).getGrid().getSearchBoxMode());
    }

    @Override
    protected void renderButtonIcon(int x, int y) {
        int mode = ((GridScreen) screen).getGrid().getSearchBoxMode();

        screen.blit(x, y, mode == IGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED || mode == IGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED ? 16 : 0, 96, 16, 16);
    }

    @Override
    public void onPress() {
        int mode = ((GridScreen) screen).getGrid().getSearchBoxMode();

        if (mode == IGrid.SEARCH_BOX_MODE_NORMAL) {
            mode = IGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED;
        } else if (mode == IGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED) {
            if (JeiIntegration.isLoaded()) {
                mode = IGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED;
            } else {
                mode = IGrid.SEARCH_BOX_MODE_NORMAL;
            }
        } else if (mode == IGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED) {
            mode = IGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED;
        } else if (mode == IGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED) {
            mode = IGrid.SEARCH_BOX_MODE_NORMAL;
        }

        ((GridScreen) screen).getGrid().onSearchBoxModeChanged(mode);

        ((GridScreen) screen).getSearchField().setMode(mode);
    }
}
