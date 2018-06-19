package com.raoulvdberge.refinedstorage.apiimpl.autocrafting.craftingmonitor;

import com.raoulvdberge.refinedstorage.api.autocrafting.craftingmonitor.ICraftingMonitorElement;
import com.raoulvdberge.refinedstorage.api.render.IElementDrawers;
import com.raoulvdberge.refinedstorage.util.RenderUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CraftingMonitorElementText implements ICraftingMonitorElement {
    public static final String ID = "text";

    private String text;
    private int offset;

    public CraftingMonitorElementText(String text, int offset) {
        this.text = text;
        this.offset = offset;
    }

    public String getText() {
        return text;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(int x, int y, IElementDrawers drawers, boolean selected) {
        if (selected) {
            drawers.getOverlayDrawer().draw(x, y, 0xFFCCCCCC);
        }

        float scale = drawers.getFontRenderer().getUnicodeFlag() ? 1F : 0.5F;

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);

        drawers.getStringDrawer().draw(RenderUtils.getOffsetOnScale(x + offset, scale), RenderUtils.getOffsetOnScale(y + 7, scale), I18n.format(text));

        GlStateManager.popMatrix();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, text);
        buf.writeInt(offset);
    }

    @Override
    public boolean merge(ICraftingMonitorElement element) {
        return element.getId().equals(getId()) && elementHashCode() == element.elementHashCode();
    }

    @Override
    public int elementHashCode() {
        return text.hashCode();
    }
}
