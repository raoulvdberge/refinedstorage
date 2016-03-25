package refinedstorage.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import refinedstorage.container.ContainerController;
import refinedstorage.gui.sidebutton.SideButtonRedstoneMode;
import refinedstorage.tile.TileController;
import refinedstorage.tile.TileMachine;

public class GuiController extends GuiBase {
    private TileController controller;

    private int barX = 8;
    private int barY = 20;
    private int barWidth = 16;
    private int barHeight = 59;

    private Scrollbar scrollbar = new Scrollbar(157, 20, 12, 59);

    public GuiController(ContainerController container, TileController controller) {
        super(container, 176, 181);

        this.controller = controller;
    }

    @Override
    public void init(int x, int y) {
        addSideButton(new SideButtonRedstoneMode(controller));
    }

    @Override
    public void update(int x, int y) {
        scrollbar.setCanScroll(getRows() > getVisibleRows());
    }

    @Override
    public void drawBackground(int x, int y, int mouseX, int mouseY) {
        bindTexture("gui/controller.png");

        drawTexture(x, y, 0, 0, width, height);

        int barHeightNew = (int) ((float) controller.getEnergyStored(null) / (float) controller.getMaxEnergyStored(null) * (float) barHeight);

        drawTexture(x + barX, y + barY + barHeight - barHeightNew, 178, 0 + (barHeight - barHeightNew), barWidth, barHeightNew);

        scrollbar.draw(this);
    }

    private int calculateOffsetOnScale(int pos ,float scale) {
    	float multiplier = (pos / scale);
    	return (int)multiplier;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        scrollbar.update(this, mouseX, mouseY);

        drawString(7, 7, t("gui.refinedstorage:controller." + controller.getType().getId()));
        drawString(7, 87, t("container.inventory"));

        int x = 33;
        int y = 26;

        int slot = getOffset() * 2;

        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < 4; ++i) {
            if (slot < controller.getMachines().size()) {
                TileMachine machine = controller.getMachines().get(slot);
                IBlockState machineState = machine.getWorld().getBlockState(machine.getPos());
                Block machineBlock = machineState.getBlock();

                ItemStack machineStack = new ItemStack(machineBlock, 1, machineBlock.getMetaFromState(machineState));

                drawItem(x, y + 5, machineStack);

                GlStateManager.pushMatrix();
                float scale = 0.5f;

                GlStateManager.scale(scale, scale, 1);
                drawString(calculateOffsetOnScale(x + 1, scale), calculateOffsetOnScale(y - 3, scale), machineStack.getDisplayName());
                drawString(calculateOffsetOnScale(x + 21, scale), calculateOffsetOnScale(y + 10, scale), t("misc.refinedstorage:energy_usage_minimal", machine.getEnergyUsage()));
                GlStateManager.popMatrix();
            }

            if (i == 1) {
                x = 33;
                y += 30;
            } else {
                x += 60;
            }

            slot++;
        }

        if (inBounds(barX, barY, barWidth, barHeight, mouseX, mouseY)) {
            drawTooltip(mouseX, mouseY, t("misc.refinedstorage:energy_usage", controller.getEnergyUsage()) + "\n" + t("misc.refinedstorage:energy_stored", controller.getEnergyStored(null), controller.getMaxEnergyStored(null)));
        }
    }

    public int getOffset() {
        return (int) (scrollbar.getCurrentScroll() / 59f * (float) getRows());
    }

    private int getRows() {
        int max = (int) Math.ceil((float) controller.getMachines().size() / (float) 2);

        return max < 0 ? 0 : max;
    }

    private int getVisibleRows() {
        return 2;
    }
}
