package com.refinedmods.refinedstorage.screen.widget.sidebutton;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class GridSortingDirectionSideButton extends SideButton {
    private IGrid grid;

    public GridSortingDirectionSideButton(BaseScreen screen, IGrid grid) {
        super(screen);

        this.grid = grid;
    }

    @Override
    public String getTooltip() {
        return I18n.format("sidebutton.refinedstorage.grid.sorting.direction") + "\n" + TextFormatting.GRAY + I18n.format("sidebutton.refinedstorage.grid.sorting.direction." + grid.getSortingDirection());
    }

    @Override
    protected void renderButtonIcon(int x, int y) {
        screen.blit(x, y, grid.getSortingDirection() * 16, 16, 16, 16);
    }

    @Override
    public void onPress() {
        int dir = grid.getSortingDirection();

        if (dir == IGrid.SORTING_DIRECTION_ASCENDING) {
            dir = IGrid.SORTING_DIRECTION_DESCENDING;
        } else if (dir == IGrid.SORTING_DIRECTION_DESCENDING) {
            dir = IGrid.SORTING_DIRECTION_ASCENDING;
        }

        grid.onSortingDirectionChanged(dir);
    }
}
