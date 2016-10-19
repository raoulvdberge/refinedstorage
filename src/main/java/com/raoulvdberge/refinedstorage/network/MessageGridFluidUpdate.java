package com.raoulvdberge.refinedstorage.network;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import com.raoulvdberge.refinedstorage.gui.grid.stack.ClientStackFluid;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class MessageGridFluidUpdate implements IMessage, IMessageHandler<MessageGridFluidUpdate, IMessage> {
    private INetworkMaster network;
    private List<ClientStackFluid> stacks = new ArrayList<>();

    public MessageGridFluidUpdate() {
    }

    public MessageGridFluidUpdate(INetworkMaster network) {
        this.network = network;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int items = buf.readInt();

        for (int i = 0; i < items; ++i) {
            this.stacks.add(new ClientStackFluid(RSUtils.readFluidStack(buf)));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(network.getFluidStorageCache().getList().getStacks().size());

        for (FluidStack stack : network.getFluidStorageCache().getList().getStacks()) {
            RSUtils.writeFluidStack(buf, stack);
        }
    }

    @Override
    public IMessage onMessage(MessageGridFluidUpdate message, MessageContext ctx) {
        GuiGrid.FLUIDS.clear();

        for (ClientStackFluid item : message.stacks) {
            GuiGrid.FLUIDS.put(item.getStack().getFluid(), item);
        }

        GuiGrid.markForSorting();

        return null;
    }
}
