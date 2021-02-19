package com.refinedmods.refinedstorage.block;

import com.refinedmods.refinedstorage.block.shape.ShapeCache;
import com.refinedmods.refinedstorage.container.ConstructorContainer;
import com.refinedmods.refinedstorage.container.factory.PositionalTileContainerProvider;
import com.refinedmods.refinedstorage.render.ConstantsCable;
import com.refinedmods.refinedstorage.tile.ConstructorTile;
import com.refinedmods.refinedstorage.util.BlockUtils;
import com.refinedmods.refinedstorage.util.CollisionUtils;
import com.refinedmods.refinedstorage.util.NetworkUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class ConstructorBlock extends CableBlock {
    private static final VoxelShape HEAD_NORTH = VoxelShapes.or(makeCuboidShape(2, 2, 0, 14, 14, 2), HOLDER_NORTH);
    private static final VoxelShape HEAD_EAST = VoxelShapes.or(makeCuboidShape(14, 2, 2, 16, 14, 14), HOLDER_EAST);
    private static final VoxelShape HEAD_SOUTH = VoxelShapes.or(makeCuboidShape(2, 2, 14, 14, 14, 16), HOLDER_SOUTH);
    private static final VoxelShape HEAD_WEST = VoxelShapes.or(makeCuboidShape(0, 2, 2, 2, 14, 14), HOLDER_WEST);
    private static final VoxelShape HEAD_DOWN = VoxelShapes.or(makeCuboidShape(2, 0, 2, 14, 2, 14), HOLDER_DOWN);
    private static final VoxelShape HEAD_UP = VoxelShapes.or(makeCuboidShape(2, 14, 2, 14, 16, 14), HOLDER_UP);

    public ConstructorBlock() {
        super(BlockUtils.DEFAULT_GLASS_PROPERTIES);
    }

    @Override
    public BlockDirection getDirection() {
        return BlockDirection.ANY;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ConstructorTile();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return ShapeCache.getOrCreate(state, s -> {
            VoxelShape shape = getCableShape(s);

            shape = VoxelShapes.or(shape, getHeadShape(s));

            return ConstantsCable.addCoverVoxelShapes(shape, world, pos);
        });
    }

    private VoxelShape getHeadShape(BlockState state) {
        Direction direction = state.get(getDirection().getProperty());

        if (direction == Direction.NORTH) {
            return HEAD_NORTH;
        }

        if (direction == Direction.EAST) {
            return HEAD_EAST;
        }

        if (direction == Direction.SOUTH) {
            return HEAD_SOUTH;
        }

        if (direction == Direction.WEST) {
            return HEAD_WEST;
        }

        if (direction == Direction.UP) {
            return HEAD_UP;
        }

        if (direction == Direction.DOWN) {
            return HEAD_DOWN;
        }

        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote && CollisionUtils.isInBounds(getHeadShape(state), pos, hit.getHitVec())) {
            return NetworkUtils.attemptModify(world, pos, player, () -> NetworkHooks.openGui(
                (ServerPlayerEntity) player,
                new PositionalTileContainerProvider<ConstructorTile>(
                    new TranslationTextComponent("gui.refinedstorage.constructor"),
                    (tile, windowId, inventory, p) -> new ConstructorContainer(tile, player, windowId),
                    pos
                ),
                pos
            ));
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasConnectedState() {
        return true;
    }
}
