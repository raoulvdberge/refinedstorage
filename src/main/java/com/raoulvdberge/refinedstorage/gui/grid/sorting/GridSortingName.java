package com.raoulvdberge.refinedstorage.gui.grid.sorting;

import com.raoulvdberge.refinedstorage.gui.grid.stack.IClientStack;
import com.raoulvdberge.refinedstorage.tile.grid.TileGrid;

public class GridSortingName extends GridSorting {
    @Override
    public int compare(IClientStack left, IClientStack right) {
        String leftName = left.getName();
        String rightName = right.getName();

        if (sortingDirection == TileGrid.SORTING_DIRECTION_ASCENDING) {
            return leftName.compareTo(rightName);
        } else if (sortingDirection == TileGrid.SORTING_DIRECTION_DESCENDING) {
            return rightName.compareTo(leftName);
        }

        return 0;
    }
}
