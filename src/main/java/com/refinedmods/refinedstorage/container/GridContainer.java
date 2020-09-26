package com.refinedmods.refinedstorage.container;

import com.refinedmods.refinedstorage.RSContainers;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.ICraftingGridListener;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCache;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCacheListener;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.container.slot.grid.CraftingGridSlot;
import com.refinedmods.refinedstorage.container.slot.grid.ResultCraftingGridSlot;
import com.refinedmods.refinedstorage.container.slot.legacy.LegacyBaseSlot;
import com.refinedmods.refinedstorage.container.slot.legacy.LegacyDisabledSlot;
import com.refinedmods.refinedstorage.container.slot.legacy.LegacyFilterSlot;
import com.refinedmods.refinedstorage.screen.IScreenInfoProvider;
import com.refinedmods.refinedstorage.tile.BaseTile;
import com.refinedmods.refinedstorage.tile.config.IType;
import com.refinedmods.refinedstorage.tile.grid.portable.IPortableGrid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GridContainer extends BaseContainer implements ICraftingGridListener {
    private final IGrid grid;
    private IStorageCache cache;
    private IStorageCacheListener listener;
    private IScreenInfoProvider screenInfoProvider;

    private ResultCraftingGridSlot craftingResultSlot;
    private LegacyBaseSlot patternResultSlot;
    private List<Slot> itemPatternSlots = new ArrayList<>();
    private List<Slot> fluidPatternSlots = new ArrayList<>();
    private int patternScrollOffset;

    public GridContainer(IGrid grid, @Nullable BaseTile gridTile, PlayerEntity player, int windowId) {
        super(RSContainers.GRID, gridTile, player, windowId);

        this.grid = grid;

        grid.addCraftingListener(this);
    }

    public IScreenInfoProvider getScreenInfoProvider() {
        return screenInfoProvider;
    }

    public void setScreenInfoProvider(IScreenInfoProvider screenInfoProvider) {
        this.screenInfoProvider = screenInfoProvider;
    }

    public void initSlots() {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.transferManager.clearTransfers();

        addFilterSlots();

        if (grid instanceof IPortableGrid) {
            addPortableGridSlots();
        }

        if (grid.getGridType() == GridType.CRAFTING) {
            addCraftingSlots();
        } else if (grid.getGridType() == GridType.PATTERN) {
            addPatternSlots();
        }

        transferManager.setNotFoundHandler(slotIndex -> {
            if (!getPlayer().getEntityWorld().isRemote) {
                Slot slot = inventorySlots.get(slotIndex);
                if (grid instanceof IPortableGrid && slot instanceof SlotItemHandler) {
                    if (((SlotItemHandler) slot).getItemHandler().equals(((IPortableGrid) grid).getDisk())) {
                        return ItemStack.EMPTY;
                    }
                }
                if (slot.getHasStack()) {
                    if (slot == craftingResultSlot) {
                        grid.onCraftedShift(getPlayer());

                        detectAndSendChanges();
                    } else {
                        ItemStack stack = slot.getStack();

                        if (grid.getGridType() == GridType.FLUID) {
                            IFluidGridHandler fluidHandler = grid.getFluidHandler();

                            if (fluidHandler != null) {
                                slot.putStack(fluidHandler.onInsert((ServerPlayerEntity) getPlayer(), stack));
                            }
                        } else {
                            IItemGridHandler itemHandler = grid.getItemHandler();

                            if (itemHandler != null) {
                                slot.putStack(itemHandler.onInsert((ServerPlayerEntity) getPlayer(), stack));
                            } else if (slot instanceof CraftingGridSlot && mergeItemStack(stack, 14, 14 + (9 * 4), false)) {
                                slot.onSlotChanged();

                                // This is needed because when a grid is disconnected,
                                // and a player shift clicks from the matrix to the inventory (this if case),
                                // the crafting inventory isn't being notified.
                                grid.onCraftingMatrixChanged();
                            }
                        }

                        detectAndSendChanges();
                    }
                }
            }

            return ItemStack.EMPTY;
        });

        addPlayerInventory(8, screenInfoProvider.getYPlayerInventory());
    }

    private void addPortableGridSlots() {
        addSlot(new SlotItemHandler(((IPortableGrid) grid).getDisk(), 0, 204, 6));

        transferManager.addBiTransfer(getPlayer().inventory, ((IPortableGrid) grid).getDisk());
    }

    private void addFilterSlots() {
        int yStart = 6;

        if (grid instanceof IPortableGrid) {
            yStart = 38;
        }

        for (int i = 0; i < 4; ++i) {
            addSlot(new SlotItemHandler(grid.getFilter(), i, 204, yStart + (18 * i)));
        }

        transferManager.addBiTransfer(getPlayer().inventory, grid.getFilter());
    }

    private void addCraftingSlots() {
        int headerAndSlots = screenInfoProvider.getTopHeight() + (screenInfoProvider.getVisibleRows() * 18);

        int x = 26;
        int y = headerAndSlots + 4;

        for (int i = 0; i < 9; ++i) {
            addSlot(new CraftingGridSlot(grid.getCraftingMatrix(), i, x, y));

            x += 18;

            if ((i + 1) % 3 == 0) {
                y += 18;
                x = 26;
            }
        }

        addSlot(craftingResultSlot = new ResultCraftingGridSlot(getPlayer(), grid, 0, 130 + 4, headerAndSlots + 22));
    }

    private void addPatternSlots() {
        itemPatternSlots.clear();
        fluidPatternSlots.clear();
        int headerAndSlots = screenInfoProvider.getTopHeight() + (screenInfoProvider.getVisibleRows() * 18);

        addSlot(new SlotItemHandler(((GridNetworkNode) grid).getPatterns(), 0, 172, headerAndSlots + 4));
        addSlot(new SlotItemHandler(((GridNetworkNode) grid).getPatterns(), 1, 172, headerAndSlots + 40));

        transferManager.addBiTransfer(getPlayer().inventory, ((GridNetworkNode) grid).getPatterns());

        // Processing patterns
        int ox = 8;
        int x = ox;
        int y = headerAndSlots + 4;

        for (int i = 0; i < GridNetworkNode.PROCESSING_MATRIX_SIZE * 2; ++i) {
            int itemFilterSlotConfig = FilterSlot.FILTER_ALLOW_SIZE;
            if (i < GridNetworkNode.PROCESSING_MATRIX_SIZE) {
                itemFilterSlotConfig |= FilterSlot.FILTER_ALLOW_ALTERNATIVES;
            }

            int fluidFilterSlotConfig = FluidFilterSlot.FILTER_ALLOW_SIZE;
            if (i < GridNetworkNode.PROCESSING_MATRIX_SIZE) {
                fluidFilterSlotConfig |= FluidFilterSlot.FILTER_ALLOW_ALTERNATIVES;
            }

            int finalI = i;
            itemPatternSlots.add(addSlot(new FilterSlot(((GridNetworkNode) grid).getProcessingMatrix(), i, x, y, itemFilterSlotConfig)
                .setEnableHandler(() -> ((GridNetworkNode) grid).isProcessingPattern() && ((GridNetworkNode) grid).getType() == IType.ITEMS && isVisible(finalI))));
            fluidPatternSlots.add(addSlot(new FluidFilterSlot(((GridNetworkNode) grid).getProcessingMatrixFluids(), i, x, y, fluidFilterSlotConfig)
                .setEnableHandler(() -> ((GridNetworkNode) grid).isProcessingPattern() && ((GridNetworkNode) grid).getType() == IType.FLUIDS && isVisible(finalI))));

            x += 18;

            if ((i + 1) % 3 == 0) {
                if (i == GridNetworkNode.PROCESSING_MATRIX_SIZE - 1) {
                    ox = 93;
                    x = ox;
                    y = headerAndSlots + 4;
                } else {
                    x = ox;
                    y += 18;
                }
            }
        }

        // Regular patterns
        x = 26;
        y = headerAndSlots + 4;

        for (int i = 0; i < 9; ++i) {
            addSlot(new LegacyFilterSlot(grid.getCraftingMatrix(), i, x, y).setEnableHandler(() -> !((GridNetworkNode) grid).isProcessingPattern()));

            x += 18;

            if ((i + 1) % 3 == 0) {
                y += 18;
                x = 26;
            }
        }

        addSlot(patternResultSlot = (new LegacyDisabledSlot(grid.getCraftingResult(), 0, 134, headerAndSlots + 22).setEnableHandler(() -> !((GridNetworkNode) grid).isProcessingPattern())));
    }

    private boolean isVisible(int slotNumber) {
        return (slotNumber >= patternScrollOffset * 3
            && slotNumber < patternScrollOffset * 3 + 9)

            || (slotNumber >= patternScrollOffset * 3 + GridNetworkNode.PROCESSING_MATRIX_SIZE
            && slotNumber < patternScrollOffset * 3 + GridNetworkNode.PROCESSING_MATRIX_SIZE + 9);
    }

    public IGrid getGrid() {
        return grid;
    }

    @Override
    public void onCraftingMatrixChanged() {
        for (int i = 0; i < inventorySlots.size(); ++i) {
            Slot slot = inventorySlots.get(i);

            if (slot instanceof CraftingGridSlot || slot == craftingResultSlot || slot == patternResultSlot) {
                for (IContainerListener listener : listeners) {
                    // @Volatile: We can't use IContainerListener#sendSlotContents since ServerPlayerEntity blocks CraftingResultSlot changes...
                    if (listener instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) listener).connection.sendPacket(new SSetSlotPacket(windowId, i, slot.getStack()));
                    }
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!getPlayer().world.isRemote) {
            // The grid is offline.
            if (grid.getStorageCache() == null) {
                // The grid just went offline, there is still a listener.
                if (listener != null) {
                    // Remove it from the previous cache and clean up.
                    cache.removeListener(listener);

                    listener = null;
                    cache = null;
                }
            } else if (listener == null) { // The grid came online.
                listener = grid.createListener((ServerPlayerEntity) getPlayer());
                cache = grid.getStorageCache();

                cache.addListener(listener);
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);

        if (!player.getEntityWorld().isRemote) {
            grid.onClosed(player);

            if (cache != null && listener != null) {
                cache.removeListener(listener);
            }
        }

        grid.removeCraftingListener(this);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        if (slot == craftingResultSlot || slot == patternResultSlot) {
            return false;
        }

        return super.canMergeSlot(stack, slot);
    }

    @Override
    protected int getDisabledSlotNumber() {
        return grid.getSlotId();
    }

    public void updatePatternSlotPositions(int newOffset) {
        patternScrollOffset = newOffset;
        int yPosition = screenInfoProvider.getTopHeight() + (screenInfoProvider.getVisibleRows() * 18) + 4;
        int originalYPosition = yPosition;

        for (int i = 0; i < itemPatternSlots.size(); i++) {

            if (i == GridNetworkNode.PROCESSING_MATRIX_SIZE) { // reset when reaching output slots
                yPosition = originalYPosition;
            }

            if (isVisible(i)) {
                itemPatternSlots.get(i).yPos = yPosition;
                fluidPatternSlots.get(i).yPos = yPosition;
                if ((i + 1) % 3 == 0) {
                    yPosition += 18;
                }
            }
        }
    }
}