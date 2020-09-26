package com.refinedmods.refinedstorage.screen.grid;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.RSKeyBindings;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.render.ElementDrawers;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.network.grid.*;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.IScreenInfoProvider;
import com.refinedmods.refinedstorage.screen.grid.sorting.*;
import com.refinedmods.refinedstorage.screen.grid.stack.IGridStack;
import com.refinedmods.refinedstorage.screen.grid.stack.ItemGridStack;
import com.refinedmods.refinedstorage.screen.grid.view.FluidGridView;
import com.refinedmods.refinedstorage.screen.grid.view.IGridView;
import com.refinedmods.refinedstorage.screen.grid.view.ItemGridView;
import com.refinedmods.refinedstorage.screen.widget.CheckboxWidget;
import com.refinedmods.refinedstorage.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.screen.widget.SearchWidget;
import com.refinedmods.refinedstorage.screen.widget.TabListWidget;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.*;
import com.refinedmods.refinedstorage.tile.config.IType;
import com.refinedmods.refinedstorage.tile.data.TileDataManager;
import com.refinedmods.refinedstorage.tile.grid.GridTile;
import com.refinedmods.refinedstorage.tile.grid.portable.IPortableGrid;
import com.refinedmods.refinedstorage.tile.grid.portable.PortableGridTile;
import com.refinedmods.refinedstorage.util.RenderUtils;
import com.refinedmods.refinedstorage.util.TimeUtils;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class GridScreen extends BaseScreen<GridContainer> implements IScreenInfoProvider {
    private static String searchQuery = "";

    private IGridView view;

    private SearchWidget searchField;
    private CheckboxWidget exactPattern;
    private CheckboxWidget processingPattern;

    private ScrollbarWidget scrollbar;
    private ScrollbarWidget patternScrollbar;

    private final IGrid grid;
    private final TabListWidget tabs;

    private boolean wasConnected;
    private boolean doSort;

    private final int patternScrollOffsetAbsoluteMax = GridNetworkNode.PROCESSING_MATRIX_SIZE / 3 - 3;

    private int slotNumber;
    private int patternScrollOffset;
    private int patternScrollOffsetMax;
    private boolean updatePatternOffset;

    public GridScreen(GridContainer container, IGrid grid, PlayerInventory inventory, ITextComponent title) {
        super(container, 227, 0, inventory, title);

        this.grid = grid;
        this.view = grid.getGridType() == GridType.FLUID ? new FluidGridView(this, getDefaultSorter(), getSorters()) : new ItemGridView(this, getDefaultSorter(), getSorters());
        this.wasConnected = this.grid.isGridActive();
        this.tabs = new TabListWidget(this, new ElementDrawers(this, font), grid::getTabs, grid::getTotalTabPages, grid::getTabPage, grid::getTabSelected, IGrid.TABS_PER_PAGE);
        this.tabs.addListener(new TabListWidget.ITabListListener() {
            @Override
            public void onSelectionChanged(int tab) {
                grid.onTabSelectionChanged(tab);
            }

            @Override
            public void onPageChanged(int page) {
                grid.onTabPageChanged(page);
            }
        });

        if (grid instanceof GridNetworkNode && grid.getGridType() == GridType.PATTERN) {
            GridNetworkNode node = (GridNetworkNode) grid;
            node.getProcessingMatrix().addListener((handler, slot, reading) -> updatePatternOffset = true);
            node.getProcessingMatrixFluids().addListener((handler, slot, reading) -> updatePatternOffset = true);
        }
    }

    @Override
    protected void onPreInit() {
        super.onPreInit();
        this.doSort = true;
        this.ySize = getTopHeight() + getBottomHeight() + (getVisibleRows() * 18);
    }

    @Override
    public void onPostInit(int x, int y) {
        this.container.initSlots();

        this.tabs.init(xSize - 32);

        this.scrollbar = new ScrollbarWidget(this, 174, getTopHeight(), 12, (getVisibleRows() * 18) - 2);

        if (grid instanceof GridNetworkNode || grid instanceof PortableGridTile) {
            addSideButton(new RedstoneModeSideButton(this, grid instanceof GridNetworkNode ? GridTile.REDSTONE_MODE : PortableGridTile.REDSTONE_MODE));
        }

        int sx = x + 80 + 1;
        int sy = y + 6 + 1;

        if (searchField == null) {
            searchField = new SearchWidget(font, sx, sy, 88 - 6);
            searchField.setResponder(value -> {
                searchField.updateJei();

                getView().sort(); // Use getter since this view can be replaced.

                searchQuery = value;
            });
            searchField.setMode(grid.getSearchBoxMode());
            searchField.setText(searchQuery);
        } else {
            searchField.x = sx;
            searchField.y = sy;
        }

        addButton(searchField);

        if (grid.getViewType() != -1) {
            addSideButton(new GridViewTypeSideButton(this, grid));
        }

        addSideButton(new GridSortingDirectionSideButton(this, grid));
        addSideButton(new GridSortingTypeSideButton(this, grid));
        addSideButton(new GridSearchBoxModeSideButton(this));
        addSideButton(new GridSizeSideButton(this, () -> grid.getSize(), size -> grid.onSizeChanged(size)));

        if (grid.getGridType() == GridType.PATTERN) {
            patternScrollbar = new ScrollbarWidget(this, 160, getTopHeight() + getVisibleRows() * 18 + 4, 6, 18 * 3 - 2, true);

            patternScrollbar.setMaxOffset(patternScrollOffsetAbsoluteMax);
            patternScrollbar.setOffset(patternScrollOffset); // keep offset when changing between fluid and item view

            container.updatePatternSlotPositions(patternScrollOffset);
            patternScrollbar.addListener((oldOffset, newOffset) -> {
                patternScrollOffset = newOffset;
                container.updatePatternSlotPositions(newOffset);
            });

            processingPattern = addCheckBox(x + 7, y + getTopHeight() + (getVisibleRows() * 18) + 60, new TranslationTextComponent("misc.refinedstorage.processing"), GridTile.PROCESSING_PATTERN.getValue(), btn -> {
                // Rebuild the inventory slots before the slot change packet arrives.
                GridTile.PROCESSING_PATTERN.setValue(false, processingPattern.isChecked());
                ((GridNetworkNode) grid).clearMatrix(); // The server does this but let's do it earlier so the client doesn't notice.
                this.container.initSlots();

                patternScrollOffset = 0; // reset offset when switching between crafting and processing
                TileDataManager.setParameter(GridTile.PROCESSING_PATTERN, processingPattern.isChecked());
            });

            if (!processingPattern.isChecked()) {
                exactPattern = addCheckBox(
                    processingPattern.x + processingPattern.getWidth() + 5,
                    y + getTopHeight() + (getVisibleRows() * 18) + 60,
                    new TranslationTextComponent("misc.refinedstorage.exact"),
                    GridTile.EXACT_PATTERN.getValue(),
                    btn -> TileDataManager.setParameter(GridTile.EXACT_PATTERN, exactPattern.isChecked())
                );
                patternScrollbar.setEnabled(false);
            } else {
                patternScrollbar.setEnabled(true);
            }

            addSideButton(new TypeSideButton(this, GridTile.PROCESSING_TYPE) {
                @Override
                public void onPress() {
                    super.onPress();
                    updatePatternOffsetMax(GridTile.PROCESSING_TYPE.getValue() != IType.ITEMS);
                }
            });
        }

        updateScrollbar();
    }

    public IGrid getGrid() {
        return grid;
    }

    public void setView(IGridView view) {
        this.view = view;
    }

    public IGridView getView() {
        return view;
    }

    @Override
    public void tick(int x, int y) {
        if (wasConnected != grid.isGridActive()) {
            wasConnected = grid.isGridActive();

            view.sort();
        }

        if (updatePatternOffset) {
            updatePatternOffsetMax(GridTile.PROCESSING_TYPE.getValue() == IType.ITEMS);
            updatePatternOffset = false;
        }

        if (isKeyDown(RSKeyBindings.CLEAR_GRID_CRAFTING_MATRIX)) {
            RS.NETWORK_HANDLER.sendToServer(new GridClearMessage());
        }

        tabs.update();
    }

    @Override
    public int getTopHeight() {
        return 19;
    }

    @Override
    public int getBottomHeight() {
        if (grid.getGridType() == GridType.CRAFTING) {
            return 156;
        } else if (grid.getGridType() == GridType.PATTERN) {
            return 169;
        } else {
            return 99;
        }
    }

    @Override
    public int getYPlayerInventory() {
        int yp = getTopHeight() + (getVisibleRows() * 18);

        if (grid.getGridType() == GridType.NORMAL || grid.getGridType() == GridType.FLUID) {
            yp += 16;
        } else if (grid.getGridType() == GridType.CRAFTING) {
            yp += 73;
        } else if (grid.getGridType() == GridType.PATTERN) {
            yp += 86;
        }

        return yp;
    }

    @Override
    public int getRows() {
        return Math.max(0, (int) Math.ceil((float) view.getStacks().size() / 9F));
    }

    @Override
    public int getCurrentOffset() {
        return scrollbar.getOffset();
    }

    @Override
    public String getSearchFieldText() {
        return searchField.getText();
    }

    @Override
    public int getVisibleRows() {
        switch (grid.getSize()) {
            case IGrid.SIZE_STRETCH:
                int screenSpaceAvailable = height - getTopHeight() - getBottomHeight();

                return Math.max(3, Math.min((screenSpaceAvailable / 18) - 3, RS.CLIENT_CONFIG.getGrid().getMaxRowsStretch()));
            case IGrid.SIZE_SMALL:
                return 3;
            case IGrid.SIZE_MEDIUM:
                return 5;
            case IGrid.SIZE_LARGE:
                return 8;
            default:
                return 3;
        }
    }

    private boolean isOverSlotWithStack() {
        return grid.isGridActive() && isOverSlot() && slotNumber < view.getStacks().size();
    }

    private boolean isOverSlot() {
        return slotNumber >= 0;
    }

    public boolean isOverSlotArea(double mouseX, double mouseY) {
        return RenderUtils.inBounds(7, 19, 162, 18 * getVisibleRows(), mouseX, mouseY);
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    private boolean isOverClear(double mouseX, double mouseY) {
        int y = getTopHeight() + (getVisibleRows() * 18) + 4;

        switch (grid.getGridType()) {
            case CRAFTING:
                return RenderUtils.inBounds(82, y, 7, 7, mouseX, mouseY);
            case PATTERN:
                if (((GridNetworkNode) grid).isProcessingPattern()) {
                    return RenderUtils.inBounds(154, y, 7, 7, mouseX, mouseY);
                }

                return RenderUtils.inBounds(82, y, 7, 7, mouseX, mouseY);
            default:
                return false;
        }
    }

    private boolean isOverCreatePattern(double mouseX, double mouseY) {
        return grid.getGridType() == GridType.PATTERN && RenderUtils.inBounds(172, getTopHeight() + (getVisibleRows() * 18) + 22, 16, 16, mouseX, mouseY) && ((GridNetworkNode) grid).canCreatePattern();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY) {
        tabs.drawBackground(matrixStack, x, y - tabs.getHeight());

        if (grid instanceof IPortableGrid) {
            bindTexture(RS.ID, "gui/portable_grid.png");
        } else if (grid.getGridType() == GridType.CRAFTING) {
            bindTexture(RS.ID, "gui/crafting_grid.png");
        } else if (grid.getGridType() == GridType.PATTERN) {
            bindTexture(RS.ID, "gui/pattern_grid" + (((GridNetworkNode) grid).isProcessingPattern() ? "_processing" : "") + ".png");
        } else {
            bindTexture(RS.ID, "gui/grid.png");
        }

        int yy = y;

        blit(matrixStack, x, yy, 0, 0, xSize - 34, getTopHeight());

        // Filters and/or portable grid disk
        blit(matrixStack, x + xSize - 34 + 4, y, 197, 0, 30, grid instanceof IPortableGrid ? 114 : 82);

        int rows = getVisibleRows();

        for (int i = 0; i < rows; ++i) {
            yy += 18;

            blit(matrixStack, x, yy, 0, getTopHeight() + (i > 0 ? (i == rows - 1 ? 18 * 2 : 18) : 0), xSize - 34, 18);
        }

        yy += 18;

        blit(matrixStack, x, yy, 0, getTopHeight() + (18 * 3), xSize - 34, getBottomHeight());

        if (grid.getGridType() == GridType.PATTERN) {
            int ty = 0;

            if (isOverCreatePattern(mouseX - guiLeft, mouseY - guiTop)) {
                ty = 1;
            }

            if (!((GridNetworkNode) grid).canCreatePattern()) {
                ty = 2;
            }

            blit(matrixStack, x + 172, y + getTopHeight() + (getVisibleRows() * 18) + 22, 240, ty * 16, 16, 16);
            if (processingPattern.isChecked()) {
                updatePatternScrollbar();
                patternScrollbar.render(matrixStack);
            }
        }

        tabs.drawForeground(matrixStack, x, y - tabs.getHeight(), mouseX, mouseY, true);

        searchField.render(matrixStack, 0, 0, 0);

        scrollbar.render(matrixStack);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Drawn in here for bug #1844 (https://github.com/refinedmods/refinedstorage/issues/1844)
        // Item tooltips can't be rendered in the foreground layer due to the X offset translation.
        if (isOverSlotWithStack()) {
            drawGridTooltip(matrixStack, view.getStacks().get(slotNumber), mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY) {
        renderString(matrixStack, 7, 7, title.getString());
        renderString(matrixStack, 7, getYPlayerInventory() - 12, I18n.format("container.inventory"));

        int x = 8;
        int y = 19;

        this.slotNumber = -1;

        int slot = scrollbar != null ? (scrollbar.getOffset() * 9) : 0;

        RenderHelper.setupGui3DDiffuseLighting();

        for (int i = 0; i < 9 * getVisibleRows(); ++i) {
            if (RenderUtils.inBounds(x, y, 16, 16, mouseX, mouseY) || !grid.isGridActive()) {
                this.slotNumber = slot;
            }

            if (slot < view.getStacks().size()) {
                view.getStacks().get(slot).draw(matrixStack, this, x, y);
            }

            if (RenderUtils.inBounds(x, y, 16, 16, mouseX, mouseY) || !grid.isGridActive()) {
                int color = grid.isGridActive() ? -2130706433 : 0xFF5B5B5B;

                matrixStack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                fillGradient(matrixStack, x, y, x + 16, y + 16, color, color);
                RenderSystem.colorMask(true, true, true, true);
                matrixStack.pop();
            }

            slot++;

            x += 18;

            if ((i + 1) % 9 == 0) {
                x = 8;
                y += 18;
            }
        }

        if (isOverClear(mouseX, mouseY)) {
            renderTooltip(matrixStack, mouseX, mouseY, I18n.format("misc.refinedstorage.clear"));
        }

        if (isOverCreatePattern(mouseX, mouseY)) {
            renderTooltip(matrixStack, mouseX, mouseY, I18n.format("gui.refinedstorage.grid.pattern_create"));
        }

        tabs.drawTooltip(matrixStack, font, mouseX, mouseY);
    }

    private void drawGridTooltip(MatrixStack matrixStack, IGridStack gridStack, int mouseX, int mouseY) {
        List<ITextComponent> textLines = gridStack.getTooltip();
        List<String> smallTextLines = Lists.newArrayList();

        if (!gridStack.isCraftable()) {
            smallTextLines.add(I18n.format("misc.refinedstorage.total", gridStack.getFormattedFullQuantity()));
        }

        if (gridStack.getTrackerEntry() != null) {
            smallTextLines.add(TimeUtils.getAgo(gridStack.getTrackerEntry().getTime(), gridStack.getTrackerEntry().getName()));
        }

        ItemStack stack = gridStack instanceof ItemGridStack ? ((ItemGridStack) gridStack).getStack() : ItemStack.EMPTY;

        RenderUtils.drawTooltipWithSmallText(matrixStack, textLines, smallTextLines, RS.CLIENT_CONFIG.getGrid().getDetailedTooltip(), stack, mouseX, mouseY, width, height, font);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (tabs.mouseClicked()) {
            return true;
        }

        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (grid.getGridType() == GridType.PATTERN && patternScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (RS.CLIENT_CONFIG.getGrid().getPreventSortingWhileShiftIsDown()) {
            doSort = !isOverSlotArea(mouseX - guiLeft, mouseY - guiTop) && !isOverCraftingOutputArea(mouseX - guiLeft, mouseY - guiTop);
        }

        boolean clickedClear = clickedButton == 0 && isOverClear(mouseX - guiLeft, mouseY - guiTop);
        boolean clickedCreatePattern = clickedButton == 0 && isOverCreatePattern(mouseX - guiLeft, mouseY - guiTop);

        if (clickedCreatePattern) {
            minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            RS.NETWORK_HANDLER.sendToServer(new GridPatternCreateMessage(((GridNetworkNode) grid).getPos()));

            return true;
        } else if (grid.isGridActive()) {
            if (clickedClear) {
                minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                RS.NETWORK_HANDLER.sendToServer(new GridClearMessage());

                return true;
            }

            ItemStack held = container.getPlayer().inventory.getItemStack();

            if (isOverSlotArea(mouseX - guiLeft, mouseY - guiTop) && !held.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
                if (grid.getGridType() == GridType.FLUID) {
                    RS.NETWORK_HANDLER.sendToServer(new GridFluidInsertHeldMessage());
                } else {
                    RS.NETWORK_HANDLER.sendToServer(new GridItemInsertHeldMessage(clickedButton == 1));
                }

                return true;
            }

            if (isOverSlotWithStack()) {
                boolean isMiddleClickPulling = !held.isEmpty() && clickedButton == 2;
                boolean isPulling = held.isEmpty() || isMiddleClickPulling;

                IGridStack stack = view.getStacks().get(slotNumber);

                if (isPulling) {
                    if (view.canCraft() && stack.isCraftable()) {
                        minecraft.displayGuiScreen(new CraftingSettingsScreen(this, playerInventory.player, stack));
                    } else if (view.canCraft() && !stack.isCraftable() && stack.getOtherId() != null && hasShiftDown() && hasControlDown()) {
                        minecraft.displayGuiScreen(new CraftingSettingsScreen(this, playerInventory.player, view.get(stack.getOtherId())));
                    } else if (grid.getGridType() == GridType.FLUID && held.isEmpty()) {
                        RS.NETWORK_HANDLER.sendToServer(new GridFluidPullMessage(view.getStacks().get(slotNumber).getId(), hasShiftDown()));
                    } else if (grid.getGridType() != GridType.FLUID) {
                        int flags = 0;

                        if (clickedButton == 1) {
                            flags |= IItemGridHandler.EXTRACT_HALF;
                        }

                        if (hasShiftDown()) {
                            flags |= IItemGridHandler.EXTRACT_SHIFT;
                        }

                        if (clickedButton == 2) {
                            flags |= IItemGridHandler.EXTRACT_SINGLE;
                        }

                        RS.NETWORK_HANDLER.sendToServer(new GridItemPullMessage(stack.getId(), flags));
                    }
                }

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private boolean isOverCraftingOutputArea(double mouseX, double mouseY) {
        if (grid.getGridType() != GridType.CRAFTING) {
            return false;
        }
        return RenderUtils.inBounds(130, getTopHeight() + getVisibleRows() * 18 + 18, 24, 24, mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        scrollbar.mouseMoved(mx, my);

        if (grid.getGridType() == GridType.PATTERN) {
            patternScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (grid.getGridType() == GridType.PATTERN && patternScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        return scrollbar.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        if (grid.getGridType() == GridType.PATTERN && isOverPatternArea(x - guiLeft, y - guiTop) && patternScrollbar.mouseScrolled(x, y, delta)) {
            return true;
        }
        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    private boolean isOverPatternArea(double x, double y) {
        return RenderUtils.inBounds(8, getTopHeight() + getVisibleRows() * 18, 152, 54, x, y);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if (searchField.charTyped(p_charTyped_1_, p_charTyped_2_)) {
            return true;
        }

        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean keyReleased(int key, int p_223281_2_, int p_223281_3_) {
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            view.sort();
        }
        return super.keyReleased(key, p_223281_2_, p_223281_3_);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchField.keyPressed(key, scanCode, modifiers) || searchField.canWrite()) {
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();

        if (!RS.CLIENT_CONFIG.getGrid().getRememberSearchQuery()) {
            searchQuery = "";
        }
    }

    public SearchWidget getSearchField() {
        return searchField;
    }

    public void updateExactPattern(boolean checked) {
        if (exactPattern != null) {
            exactPattern.setChecked(checked);
        }
    }

    //check processing matrix and update maximum offset depending on contents
    private void updatePatternOffsetMax(boolean isItems) {
        int filledInputSlots = 0;
        int filledOutputSlots = 0;
        int lastFilledInputSlot = 0;
        int lastFilledOutputSlot = 0;

        for (int i = 0; i < GridNetworkNode.PROCESSING_MATRIX_SIZE * 2; i++) {
            if (!isMatrixSlotEmpty(isItems, i)) {
                if (i > GridNetworkNode.PROCESSING_MATRIX_SIZE - 1) {
                    filledOutputSlots++;
                    lastFilledOutputSlot = i - GridNetworkNode.PROCESSING_MATRIX_SIZE - 1;
                } else {
                    filledInputSlots++;
                    lastFilledInputSlot = i;
                }

            }
        }

        int maxFilledSlots = Math.max(filledInputSlots, filledOutputSlots);
        int maxLastFilledSlot = Math.max(lastFilledInputSlot, lastFilledOutputSlot);

        // offset calculation to show next row if 4 of 9 slots are filled.
        int filledSlotOffset = Math.floorDiv(maxFilledSlots - 1, 3);
        // offset to show the last item in the matrix
        int lastSlotOffset = Math.floorDiv(maxLastFilledSlot, 3) - 2;

        patternScrollOffsetMax = Math.max(filledSlotOffset, lastSlotOffset);
    }

    private boolean isMatrixSlotEmpty(boolean isItems, int slotNumber) {
        if (isItems) {
            return ((GridNetworkNode) grid).getProcessingMatrix().getStackInSlot(slotNumber).isEmpty();
        } else {
            return ((GridNetworkNode) grid).getProcessingMatrixFluids().getFluid(slotNumber).isEmpty();
        }
    }

    public void updatePatternScrollbar() {
        patternScrollbar.setEnabled(processingPattern.isChecked() && patternScrollOffsetMax > 0);
        int oldOffset = patternScrollbar.getOffset();
        patternScrollbar.setMaxOffset(Math.min(patternScrollOffsetMax, patternScrollOffsetAbsoluteMax));

        if (oldOffset != patternScrollbar.getOffset()) {
            container.updatePatternSlotPositions(patternScrollbar.getOffset());
        }
    }

    public void updateScrollbar() {
        scrollbar.setEnabled(getRows() > getVisibleRows());
        scrollbar.setMaxOffset(getRows() - getVisibleRows());
    }

    public boolean canSort() {
        return doSort || !hasShiftDown();
    }

    public static List<IGridSorter> getSorters() {
        List<IGridSorter> sorters = new LinkedList<>();
        sorters.add(getDefaultSorter());
        sorters.add(new QuantityGridSorter());
        sorters.add(new IdGridSorter());
        sorters.add(new LastModifiedGridSorter());
        sorters.add(new InventoryTweaksGridSorter());

        return sorters;
    }

    public static IGridSorter getDefaultSorter() {
        return new NameGridSorter();
    }
}
