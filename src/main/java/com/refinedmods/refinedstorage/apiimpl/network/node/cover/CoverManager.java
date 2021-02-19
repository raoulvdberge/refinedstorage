package com.refinedmods.refinedstorage.apiimpl.network.node.cover;

import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.node.NetworkNode;
import com.refinedmods.refinedstorage.item.CoverItem;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CoverManager {

    public static final ModelProperty<CoverManager> PROPERTY = new ModelProperty<>();

    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_ITEM = "Item";
    private static final String NBT_TYPE = "Type";

    private Map<Direction, Cover> covers = new HashMap<>();
    private NetworkNode node;

    public CoverManager(NetworkNode node) {
        this.node = node;
    }

    public boolean canConduct(Direction direction) {
        Cover cover = getCover(direction);
        if (cover != null && cover.getType() != CoverType.HOLLOW) {
            return false;
        }

        if (node.getWorld() instanceof ServerWorld){
            INetworkNode neighbor = API.instance().getNetworkNodeManager((ServerWorld) node.getWorld()).getNode(node.getPos().offset(direction));
            if (neighbor instanceof ICoverable) {
                cover = ((ICoverable) neighbor).getCoverManager().getCover(direction.getOpposite());

                if (cover != null && cover.getType() != CoverType.HOLLOW) {
                    return false;
                }
            }
        }

        return true;
    }

    @Nullable
    public Cover getCover(Direction facing) {
        return covers.get(facing);
    }

    public boolean hasCover(Direction facing) {
        return covers.containsKey(facing);
    }

    public boolean setCover(Direction facing, @Nullable Cover cover) {
        if (cover == null || (isValidCover(cover.getStack()) && !hasCover(facing))) {
            if (cover != null) {
                if (!(node instanceof CableNetworkNode) && facing == node.getDirection() && cover.getType() != CoverType.HOLLOW) {
                    return false;
                }
            }

            if (cover == null) {
                covers.remove(facing);
            } else {
                covers.put(facing, cover);
            }

            node.markDirty();

            if (node.getNetwork() != null) {
                node.getNetwork().getNodeGraph().invalidate(Action.PERFORM, node.getNetwork().getWorld(), node.getNetwork().getPosition());
            }

            return true;
        }

        return false;
    }

    public void readFromNbt(CompoundNBT nbt) {
        covers.clear();
        System.out.println(nbt);
        for (String s : nbt.keySet()) {
            CompoundNBT tag = nbt.getCompound(s);
            if (tag.contains(NBT_DIRECTION) && tag.contains(NBT_ITEM)) {
                Direction direction = Direction.byIndex(tag.getInt(NBT_DIRECTION));
                ItemStack item = ItemStack.read(tag.getCompound(NBT_ITEM));
                int type = tag.contains(NBT_TYPE) ? tag.getInt(NBT_TYPE) : 0;

                if (type >= CoverType.values().length) {
                    type = 0;
                }

                if (isValidCover(item)) {
                    covers.put(direction, new Cover(item, CoverType.values()[type]));
                }
            }
        }
    }

    public CompoundNBT writeToNbt() {
        CompoundNBT list = new CompoundNBT();

        for (Map.Entry<Direction, Cover> entry : covers.entrySet()) {
            CompoundNBT tag = new CompoundNBT();

            tag.putInt(NBT_DIRECTION, entry.getKey().ordinal());
            tag.put(NBT_ITEM, entry.getValue().getStack().serializeNBT());
            tag.putInt(NBT_TYPE, entry.getValue().getType().ordinal());

            list.put(entry.getKey().ordinal() + "",tag);
        }
        return list;
    }

    public IItemHandlerModifiable getAsInventory() {
        ItemStackHandler handler = new ItemStackHandler(covers.size());

        int i = 0;

        for (Map.Entry<Direction, Cover> entry : covers.entrySet()) {
            ItemStack cover = entry.getValue().getType().createStack();

            CoverItem.setItem(cover, entry.getValue().getStack());

            handler.setStackInSlot(i++, cover);
        }

        return handler;
    }

    @SuppressWarnings("deprecation")
    public static boolean isValidCover(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        Block block = getBlock(item);

        BlockState state = getBlockState(item);

        return block != null && state != null && ((isModelSupported(state) && !block.ticksRandomly(state) && !block.hasTileEntity(state)) || block instanceof GlassBlock || block instanceof StainedGlassBlock); //Removed is top solid as it needs world param
    }

    private static boolean isModelSupported(BlockState state) {
        if (state.getRenderType() != BlockRenderType.MODEL) {
            return false;
        }

        return state.isSolid() && state.isSolidSide(Minecraft.getInstance().world, new BlockPos(0,0,0), Direction.UP); //I dont trust this
    }

    @Nullable
    public static Block getBlock(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        Block block = Block.getBlockFromItem(item.getItem());

        if (block == Blocks.AIR) {
            return null;
        }

        return block;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static BlockState getBlockState(@Nullable ItemStack item) {
        Block block = getBlock(item);

        if (block == null) {
            return null;
        }

        try {
            return block.getDefaultState();
        } catch (Exception e) {
            return null;
        }
    }
    
}
