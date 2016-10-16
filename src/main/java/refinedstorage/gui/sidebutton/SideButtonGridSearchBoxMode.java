package refinedstorage.gui.sidebutton;

import net.minecraft.util.text.TextFormatting;
import refinedstorage.gui.GuiBase;
import refinedstorage.gui.grid.GuiGrid;
import refinedstorage.integration.jei.IntegrationJEI;
import refinedstorage.tile.grid.TileGrid;

public class SideButtonGridSearchBoxMode extends SideButton {
    public SideButtonGridSearchBoxMode(GuiGrid gui) {
        super(gui);
    }

    @Override
    public String getTooltip() {
        return TextFormatting.YELLOW + GuiBase.t("sidebutton.refinedstorage:grid.search_box_mode") + TextFormatting.RESET + "\n" + GuiBase.t("sidebutton.refinedstorage:grid.search_box_mode." + ((GuiGrid) gui).getGrid().getSearchBoxMode());
    }

    @Override
    protected void drawButtonIcon(int x, int y) {
        int mode = ((GuiGrid) gui).getGrid().getSearchBoxMode();

        gui.drawTexture(x, y, mode == TileGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED || mode == TileGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED ? 16 : 0, 96, 16, 16);
    }

    @Override
    public void actionPerformed() {
        int mode = ((GuiGrid) gui).getGrid().getSearchBoxMode();

        if (mode == TileGrid.SEARCH_BOX_MODE_NORMAL) {
            mode = TileGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED;
        } else if (mode == TileGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED) {
            if (IntegrationJEI.isLoaded()) {
                mode = TileGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED;
            } else {
                mode = TileGrid.SEARCH_BOX_MODE_NORMAL;
            }
        } else if (mode == TileGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED) {
            mode = TileGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED;
        } else if (mode == TileGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED) {
            mode = TileGrid.SEARCH_BOX_MODE_NORMAL;
        }

        ((GuiGrid) gui).getGrid().onSearchBoxModeChanged(mode);

        ((GuiGrid) gui).updateSearchFieldFocus(mode);
    }
}
