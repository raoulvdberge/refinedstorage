package refinedstorage.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.items.SlotItemHandler;
import org.lwjgl.input.Mouse;
import refinedstorage.RS;
import refinedstorage.apiimpl.storage.fluid.FluidRenderer;
import refinedstorage.gui.sidebutton.SideButton;
import refinedstorage.inventory.ItemHandlerFluid;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GuiBase extends GuiContainer {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    public static final FluidRenderer FLUID_RENDERER = new FluidRenderer(-1, 16, 16);

    private int lastButtonId;
    private int lastSideButtonY = 6;

    protected int width;
    protected int height;

    protected Scrollbar scrollbar;

    public GuiBase(Container container, int width, int height) {
        super(container);

        this.width = width;
        this.height = height;
        this.xSize = width;
        this.ySize = height;
    }

    @Override
    public void initGui() {
        super.initGui();

        lastButtonId = 0;
        lastSideButtonY = 6;

        init(guiLeft, guiTop);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        update(guiLeft, guiTop);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (scrollbar != null) {
            scrollbar.update(this, mouseX - guiLeft, mouseY - guiTop);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float renderPartialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawBackground(guiLeft, guiTop, mouseX, mouseY);

        for (int i = 0; i < inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = inventorySlots.inventorySlots.get(i);

            if (slot instanceof SlotItemHandler && ((SlotItemHandler) slot).getItemHandler() instanceof ItemHandlerFluid) {
                FluidStack stack = ((ItemHandlerFluid) ((SlotItemHandler) slot).getItemHandler()).getFluidStackInSlot(slot.getSlotIndex());

                if (stack != null) {
                    FLUID_RENDERER.draw(mc, guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, stack);
                }
            }
        }

        if (scrollbar != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            scrollbar.draw(this);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mouseX -= guiLeft;
        mouseY -= guiTop;

        String sideButtonTooltip = null;

        for (GuiButton button : buttonList) {
            if (button instanceof SideButton && ((SideButton) button).isHovered()) {
                sideButtonTooltip = ((SideButton) button).getTooltip();
            }
        }

        drawForeground(mouseX, mouseY);

        if (sideButtonTooltip != null) {
            drawTooltip(mouseX, mouseY, sideButtonTooltip);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int d = Mouse.getEventDWheel();

        if (scrollbar != null && d != 0) {
            scrollbar.wheel(d);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button instanceof SideButton) {
            ((SideButton) button).actionPerformed();
        }
    }

    public GuiButton addButton(int x, int y, int w, int h, String text) {
        return addButton(x, y, w, h, text, true);
    }

    public GuiCheckBox addCheckBox(int x, int y, String text, boolean checked) {
        GuiCheckBox checkBox = new GuiCheckBox(lastButtonId++, x, y, text, checked);

        buttonList.add(checkBox);

        return checkBox;
    }

    public GuiButton addButton(int x, int y, int w, int h, String text, boolean enabled) {
        GuiButton button = new GuiButton(lastButtonId++, x, y, w, h, text);
        button.enabled = enabled;

        buttonList.add(button);

        return button;
    }

    public SideButton addSideButton(SideButton button) {
        button.id = lastButtonId++;
        button.xPosition = guiLeft + -SideButton.WIDTH - 2;
        button.yPosition = guiTop + lastSideButtonY;

        lastSideButtonY += SideButton.HEIGHT + 2;

        buttonList.add(button);

        return button;
    }

    public boolean inBounds(int x, int y, int w, int h, int ox, int oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    public void bindTexture(String file) {
        bindTexture(RS.ID, file);
    }

    public void bindTexture(String base, String file) {
        String id = base + ":" + file;

        if (!TEXTURE_CACHE.containsKey(id)) {
            TEXTURE_CACHE.put(id, new ResourceLocation(base, "textures/" + file));
        }

        mc.getTextureManager().bindTexture(TEXTURE_CACHE.get(id));
    }

    public void drawItem(int x, int y, ItemStack stack) {
        drawItem(x, y, stack, false);
    }

    public void drawItem(int x, int y, ItemStack stack, boolean withOverlay) {
        drawItem(x, y, stack, withOverlay, null);
    }

    public void drawItem(int x, int y, ItemStack stack, boolean withOverlay, String message) {
        zLevel = 200.0F;
        itemRender.zLevel = 200.0F;

        itemRender.renderItemIntoGUI(stack, x, y);

        if (withOverlay) {
            drawItemOverlay(stack, message, x, y);
        }

        zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    public void drawItemOverlay(ItemStack stack, String text, int x, int y) {
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, stack, x, y, "");

        if (text != null) {
            drawQuantity(x, y, text);
        }
    }

    public void drawQuantity(int x, int y, String qty) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 1);
        GlStateManager.scale(0.5f, 0.5f, 1);

        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableDepth();

        fontRendererObj.drawStringWithShadow(qty, 30 - fontRendererObj.getStringWidth(qty), 22, 16777215);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void drawString(int x, int y, String message) {
        drawString(x, y, message, 4210752);
    }

    public void drawString(int x, int y, String message, int color) {
        GlStateManager.disableLighting();
        fontRendererObj.drawString(message, x, y, color);
        GlStateManager.enableLighting();
    }

    public void drawTooltip(int x, int y, String message) {
        drawTooltip(x, y, Arrays.asList(message.split("\n")));
    }

    public void drawTooltip(int x, int y, List<String> lines) {
        GlStateManager.disableLighting();
        drawHoveringText(lines, x, y);
        GlStateManager.enableLighting();
    }

    public void drawTexture(int x, int y, int textureX, int textureY, int width, int height) {
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    public static String t(String name, Object... format) {
        return I18n.format(name, format);
    }

    public abstract void init(int x, int y);

    public abstract void update(int x, int y);

    public abstract void drawBackground(int x, int y, int mouseX, int mouseY);

    public abstract void drawForeground(int mouseX, int mouseY);

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    public static int calculateOffsetOnScale(int pos, float scale) {
        float multiplier = (pos / scale);

        return (int) multiplier;
    }
}
