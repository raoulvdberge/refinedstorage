package com.refinedmods.refinedstorage.screen.widget.sidebutton;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class GridViewTypeSideButton extends SideButton {
    private final IGrid grid;

    public GridViewTypeSideButton(BaseScreen<GridContainer> screen, IGrid grid) {
        super(screen);

        this.grid = grid;
    }

    @Override
    public String getTooltip() {
        return I18n.format("sidebutton.refinedstorage.grid.view_type") + "\n" + TextFormatting.GRAY + I18n.format("sidebutton.refinedstorage.grid.view_type." + grid.getViewType());
    }

    @Override
    protected void renderButtonIcon(int x, int y) {
        screen.blit(x, y, (grid.getViewType() - (grid.getViewType() >= 3 ? 3 : 0)) * 16, 112, 16, 16);
    }

    @Override
    public void onPress() {
        int type = grid.getViewType();

        if (type == IGrid.VIEW_TYPE_NORMAL) {
            type = IGrid.VIEW_TYPE_NON_CRAFTABLES;
        } else if (type == IGrid.VIEW_TYPE_NON_CRAFTABLES) {
            type = IGrid.VIEW_TYPE_CRAFTABLES;
        } else if (type == IGrid.VIEW_TYPE_CRAFTABLES) {
            type = IGrid.VIEW_TYPE_NORMAL;
        }

        grid.onViewTypeChanged(type);
    }
}
