package com.refinedmods.refinedstorage.item;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.Cover;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverType;
import com.refinedmods.refinedstorage.tile.NetworkNodeTile;
import com.refinedmods.refinedstorage.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CoverItem extends Item {

    private static final String NBT_ITEM = "Item";

    public static final ItemStack HIDDEN_COVER_ALTERNATIVE = new ItemStack(Blocks.STONE_BRICKS);


    public CoverItem() {
        super(new Item.Properties().group(RS.MAIN_GROUP));
    }

    public static void setItem(ItemStack cover, ItemStack item) {
        if (!cover.hasTag()) {
            cover.setTag(new CompoundNBT());
        }

        cover.getTag().put(NBT_ITEM, item.serializeNBT());
    }

    @Nonnull
    public static ItemStack getItem(ItemStack cover) {
        if (!cover.hasTag() || !cover.getTag().contains(NBT_ITEM)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.read(cover.getTag().getCompound(NBT_ITEM));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemStack item = getItem(stack);

        if (!item.isEmpty()) {
            tooltip.add(item.getItem().getDisplayName(item));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {

        //if (RS.INSTANCE.config.hideCovers) {
        //    ItemStack stack = new ItemStack(this);

        //setItem(stack, HIDDEN_COVER_ALTERNATIVE);

        //items.add(stack);

        //return;
        //}
        if (this.isInGroup(group)) {
            for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                Item item = Item.getItemFromBlock(block);

                if (item == Items.AIR) {
                    continue;
                }

                NonNullList<ItemStack> subBlocks = NonNullList.create();

                block.fillItemGroup(ItemGroup.SEARCH, subBlocks);

                for (ItemStack subBlock : subBlocks) {
                    if (CoverManager.isValidCover(subBlock)) {
                        ItemStack stack = new ItemStack(this);

                        setItem(stack, subBlock);

                        items.add(stack);
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        World world = context.getWorld();

        ItemStack stack = context.getPlayer().getHeldItem(context.getHand());

        TileEntity tile = world.getTileEntity(pos);

        // Support placing on the bottom side without too much hassle.
        if (!canPlaceOn(tile)) {
            pos = pos.up();

            facing = Direction.DOWN;

            tile = world.getTileEntity(pos);
        }

        if (canPlaceOn(tile)) {
            if (world.isRemote) {
                ModelDataManager.requestModelDataRefresh(tile);
                return ActionResultType.SUCCESS;
            }

            INetworkNode node = ((NetworkNodeTile<?>) tile).getNode();

            if (node.getNetwork() != null && !node.getNetwork().getSecurityManager().hasPermission(Permission.BUILD, context.getPlayer())) {
                WorldUtils.sendNoPermissionMessage(context.getPlayer());

                return ActionResultType.FAIL;
            }

            if (((ICoverable) node).getCoverManager().setCover(facing, createCover(getItem(stack)))) {
                context.getPlayer().getHeldItem(context.getHand()).shrink(1);

                WorldUtils.updateBlock(world, pos);
                API.instance().getNetworkNodeManager((ServerWorld) world).markForSaving();
                return ActionResultType.SUCCESS;
            }

            return ActionResultType.FAIL;
        }

        return ActionResultType.PASS;
    }


    private boolean canPlaceOn(TileEntity tile) {
        return tile instanceof NetworkNodeTile && ((NetworkNodeTile<?>) tile).getNode() instanceof ICoverable;
    }

    protected Cover createCover(ItemStack stack) {
        return new Cover(stack, CoverType.NORMAL);
    }

}
