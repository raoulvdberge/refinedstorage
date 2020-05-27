package com.refinedmods.refinedstorage.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.integration.craftingtweaks.CraftingTweaksIntegration;
import com.refinedmods.refinedstorage.render.FluidRenderer;
import com.refinedmods.refinedstorage.render.RenderSettings;
import com.refinedmods.refinedstorage.screen.grid.AlternativesScreen;
import com.refinedmods.refinedstorage.screen.widget.CheckboxWidget;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class BaseScreen<T extends Container> extends ContainerScreen<T> {
    public static final int Z_LEVEL_ITEMS = 100;
    public static final int Z_LEVEL_TOOLTIPS = 500;
    public static final int Z_LEVEL_QTY = 300;

    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();
    private static final Map<Class, Queue<Consumer>> ACTIONS = new HashMap<>();

    private Logger logger = LogManager.getLogger(getClass());

    private int sideButtonY;

    public BaseScreen(T container, int xSize, int ySize, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);

        this.xSize = xSize;
        this.ySize = ySize;
    }

    private void runActions() {
        runActions(getClass());
        runActions(ContainerScreen.class);
    }

    private void runActions(Class clazz) {
        Queue<Consumer> queue = ACTIONS.get(clazz);

        if (queue != null && !queue.isEmpty()) {
            Consumer callback;
            while ((callback = queue.poll()) != null) {
                callback.accept(this);
            }
        }
    }

    @Override
    public void init() {
        minecraft.keyboardListener.enableRepeatEvents(true);

        onPreInit();

        super.init();

        if (CraftingTweaksIntegration.isLoaded()) {
            buttons.removeIf(b -> !CraftingTweaksIntegration.isCraftingTweaksClass(b.getClass()));
            children.removeIf(c -> !CraftingTweaksIntegration.isCraftingTweaksClass(c.getClass()));
        } else {
            buttons.clear();
            children.clear();
        }

        sideButtonY = 6;

        onPostInit(guiLeft, guiTop);

        runActions();
    }

    @Override
    public void onClose() {
        super.onClose();

        minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void tick() {
        super.tick();

        runActions();

        tick(guiLeft, guiTop);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        super.render(mouseX, mouseY, partialTicks);

        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float renderPartialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        renderBackground(guiLeft, guiTop, mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            Slot slot = container.inventorySlots.get(i);

            if (slot.isEnabled() && slot instanceof FluidFilterSlot) {
                FluidStack stack = ((FluidFilterSlot) slot).getFluidInventory().getFluid(slot.getSlotIndex());

                if (!stack.isEmpty()) {
                    FluidRenderer.INSTANCE.render(guiLeft + slot.xPos, guiTop + slot.yPos, stack);

                    if (((FluidFilterSlot) slot).isSizeAllowed()) {
                        renderQuantity(guiLeft + slot.xPos, guiTop + slot.yPos, API.instance().getQuantityFormatter().formatInBucketForm(stack.getAmount()), RenderSettings.INSTANCE.getSecondaryColor());

                        GL11.glDisable(GL11.GL_LIGHTING);
                    }
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        mouseX -= guiLeft;
        mouseY -= guiTop;

        renderForeground(mouseX, mouseY);

        for (int i = 0; i < this.buttons.size(); ++i) {
            Widget button = buttons.get(i);

            if (button instanceof SideButton && button.isHovered()) {
                renderTooltip(mouseX, mouseY, ((SideButton) button).getTooltip());
            }
        }

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            Slot slot = container.inventorySlots.get(i);

            if (slot.isEnabled() && slot instanceof FluidFilterSlot) {
                FluidStack stack = ((FluidFilterSlot) slot).getFluidInventory().getFluid(slot.getSlotIndex());

                if (!stack.isEmpty() && RenderUtils.inBounds(slot.xPos, slot.yPos, 17, 17, mouseX, mouseY)) {
                    renderTooltip(mouseX, mouseY, stack.getDisplayName().getFormattedText());
                }
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        boolean valid = type != ClickType.QUICK_MOVE && minecraft.player.inventory.getItemStack().isEmpty();

        if (valid && slot instanceof FilterSlot && slot.isEnabled() && ((FilterSlot) slot).isSizeAllowed()) {
            if (!slot.getStack().isEmpty()) {
                if (((FilterSlot) slot).isAlternativesAllowed() && hasControlDown()) {
                    minecraft.displayGuiScreen(new AlternativesScreen(
                        this,
                        minecraft.player,
                        new TranslationTextComponent("gui.refinedstorage.alternatives"),
                        slot.getStack(),
                        slot.getSlotIndex()
                    ));
                } else {
                    minecraft.displayGuiScreen(new ItemAmountScreen(
                        this,
                        minecraft.player,
                        slot.slotNumber,
                        slot.getStack(),
                        slot.getSlotStackLimit(),
                        ((FilterSlot) slot).isAlternativesAllowed() ? (parent -> new AlternativesScreen(
                            parent,
                            minecraft.player,
                            new TranslationTextComponent("gui.refinedstorage.alternatives"),
                            slot.getStack(),
                            slot.getSlotIndex()
                        )) : null
                    ));
                }
            }
        } else if (valid && slot instanceof FluidFilterSlot && slot.isEnabled() && ((FluidFilterSlot) slot).isSizeAllowed()) {
            FluidStack stack = ((FluidFilterSlot) slot).getFluidInventory().getFluid(slot.getSlotIndex());

            if (!stack.isEmpty()) {
                if (((FluidFilterSlot) slot).isAlternativesAllowed() && hasControlDown()) {
                    minecraft.displayGuiScreen(new AlternativesScreen(
                        this,
                        minecraft.player,
                        new TranslationTextComponent("gui.refinedstorage.alternatives"),
                        stack,
                        slot.getSlotIndex()
                    ));
                } else {
                    minecraft.displayGuiScreen(new FluidAmountScreen(
                        this,
                        minecraft.player,
                        slot.slotNumber,
                        stack,
                        ((FluidFilterSlot) slot).getFluidInventory().getMaxAmount(),
                        ((FluidFilterSlot) slot).isAlternativesAllowed() ? (parent -> new AlternativesScreen(
                            this,
                            minecraft.player,
                            new TranslationTextComponent("gui.refinedstorage.alternatives"),
                            stack,
                            slot.getSlotIndex()
                        )) : null
                    ));
                }
            } else {
                super.handleMouseClick(slot, slotId, mouseButton, type);
            }
        } else {
            super.handleMouseClick(slot, slotId, mouseButton, type);
        }
    }

    public CheckboxWidget addCheckBox(int x, int y, String text, boolean checked, Consumer<CheckboxButton> onPress) {
        CheckboxWidget checkBox = new CheckboxWidget(x, y, text, checked, onPress);

        this.addButton(checkBox);

        return checkBox;
    }

    public Button addButton(int x, int y, int w, int h, String text, boolean enabled, boolean visible, Button.IPressable onPress) {
        Button button = new Button(x, y, w, h, text, onPress);

        button.active = enabled;
        button.visible = visible;

        this.addButton(button);

        return button;
    }

    public SideButton addSideButton(SideButton button) {
        button.x = guiLeft + -SideButton.WIDTH - 2;
        button.y = guiTop + sideButtonY;

        sideButtonY += SideButton.HEIGHT + 2;

        this.addButton(button);

        return button;
    }

    public void bindTexture(String namespace, String filenameInTexturesFolder) {
        minecraft.getTextureManager().bindTexture(TEXTURE_CACHE.computeIfAbsent(namespace + ":" + filenameInTexturesFolder, (newId) -> new ResourceLocation(namespace, "textures/" + filenameInTexturesFolder)));
    }

    public void renderItem(int x, int y, ItemStack stack) {
        renderItem(x, y, stack, false, null, 0);
    }

    public void renderItem(int x, int y, ItemStack stack, boolean overlay, @Nullable String text, int textColor) {
        try {
            setBlitOffset(Z_LEVEL_ITEMS);
            itemRenderer.zLevel = Z_LEVEL_ITEMS;

            itemRenderer.renderItemIntoGUI(stack, x, y);

            if (overlay) {
                itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, "");
            }

            setBlitOffset(0);
            itemRenderer.zLevel = 0;

            if (text != null) {
                renderQuantity(x, y, text, textColor);
            }
        } catch (Throwable t) {
            logger.warn("Couldn't render stack: " + stack.getItem().toString(), t);
        }
    }

    public void renderQuantity(int x, int y, String qty, int color) {
        boolean large = minecraft.getForceUnicodeFont() || RS.CLIENT_CONFIG.getGrid().getLargeFont();

        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, Z_LEVEL_QTY);

        if (!large) {
            RenderSystem.scalef(0.5f, 0.5f, 1);
        }

        font.drawStringWithShadow(qty, (large ? 16 : 30) - font.getStringWidth(qty), large ? 8 : 22, color);

        RenderSystem.popMatrix();
    }

    public void renderString(int x, int y, String message) {
        renderString(x, y, message, RenderSettings.INSTANCE.getPrimaryColor());
    }

    public void renderString(int x, int y, String message, int color) {
        font.drawString(message, x, y, color);
    }

    public void renderTooltip(int x, int y, String lines) {
        renderTooltip(ItemStack.EMPTY, x, y, lines);
    }

    public void renderTooltip(@Nonnull ItemStack stack, int x, int y, String lines) {
        renderTooltip(stack, x, y, Arrays.asList(lines.split("\n")));
    }

    public void renderTooltip(@Nonnull ItemStack stack, int x, int y, List<String> lines) {
        GuiUtils.drawHoveringText(stack, lines, x, y, width, height, -1, font);
    }

    protected void onPreInit() {
        // NO OP
    }

    public static boolean isKeyDown(KeyBinding keybinding) {
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keybinding.getKey().getKeyCode()) &&
            keybinding.getKeyConflictContext().isActive() &&
            keybinding.getKeyModifier().isActive(keybinding.getKeyConflictContext());
    }

    public abstract void onPostInit(int x, int y);

    public abstract void tick(int x, int y);

    public abstract void renderBackground(int x, int y, int mouseX, int mouseY);

    public abstract void renderForeground(int mouseX, int mouseY);

    public static <T> void executeLater(Class<T> clazz, Consumer<T> callback) {
        Queue<Consumer> queue = ACTIONS.get(clazz);

        if (queue == null) {
            ACTIONS.put(clazz, queue = new ArrayDeque<>());
        }

        queue.add(callback);
    }

    public static void executeLater(Consumer<ContainerScreen> callback) {
        executeLater(ContainerScreen.class, callback);
    }
}
