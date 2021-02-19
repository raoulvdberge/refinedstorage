package com.refinedmods.refinedstorage.block;

import com.refinedmods.refinedstorage.block.shape.ShapeCache;
import com.refinedmods.refinedstorage.container.ImporterContainer;
import com.refinedmods.refinedstorage.container.factory.PositionalTileContainerProvider;
import com.refinedmods.refinedstorage.render.ConstantsCable;
import com.refinedmods.refinedstorage.tile.ImporterTile;
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

public class ImporterBlock extends CableBlock {
    private static final VoxelShape LINE_NORTH_1 = makeCuboidShape(6, 6, 4, 10, 10, 6);
    private static final VoxelShape LINE_NORTH_2 = makeCuboidShape(5, 5, 2, 11, 11, 4);
    private static final VoxelShape LINE_NORTH_3 = makeCuboidShape(3, 3, 0, 13, 13, 2);
    private static final VoxelShape LINE_NORTH = VoxelShapes.or(LINE_NORTH_1, LINE_NORTH_2, LINE_NORTH_3);

    private static final VoxelShape LINE_EAST_1 = makeCuboidShape(10, 6, 6, 12, 10, 10);
    private static final VoxelShape LINE_EAST_2 = makeCuboidShape(12, 5, 5, 14, 11, 11);
    private static final VoxelShape LINE_EAST_3 = makeCuboidShape(14, 3, 3, 16, 13, 13);
    private static final VoxelShape LINE_EAST = VoxelShapes.or(LINE_EAST_1, LINE_EAST_2, LINE_EAST_3);

    private static final VoxelShape LINE_SOUTH_1 = makeCuboidShape(6, 6, 10, 10, 10, 12);
    private static final VoxelShape LINE_SOUTH_2 = makeCuboidShape(5, 5, 12, 11, 11, 14);
    private static final VoxelShape LINE_SOUTH_3 = makeCuboidShape(3, 3, 14, 13, 13, 16);
    private static final VoxelShape LINE_SOUTH = VoxelShapes.or(LINE_SOUTH_1, LINE_SOUTH_2, LINE_SOUTH_3);

    private static final VoxelShape LINE_WEST_1 = makeCuboidShape(4, 6, 6, 6, 10, 10);
    private static final VoxelShape LINE_WEST_2 = makeCuboidShape(2, 5, 5, 4, 11, 11);
    private static final VoxelShape LINE_WEST_3 = makeCuboidShape(0, 3, 3, 2, 13, 13);
    private static final VoxelShape LINE_WEST = VoxelShapes.or(LINE_WEST_1, LINE_WEST_2, LINE_WEST_3);

    private static final VoxelShape LINE_UP_1 = makeCuboidShape(6, 10, 6, 10, 12, 10);
    private static final VoxelShape LINE_UP_2 = makeCuboidShape(5, 12, 5, 11, 14, 11);
    private static final VoxelShape LINE_UP_3 = makeCuboidShape(3, 14, 3, 13, 16, 13);
    private static final VoxelShape LINE_UP = VoxelShapes.or(LINE_UP_1, LINE_UP_2, LINE_UP_3);

    private static final VoxelShape LINE_DOWN_1 = makeCuboidShape(6, 4, 6, 10, 6, 10);
    private static final VoxelShape LINE_DOWN_2 = makeCuboidShape(5, 2, 5, 11, 4, 11);
    private static final VoxelShape LINE_DOWN_3 = makeCuboidShape(3, 0, 3, 13, 2, 13);
    private static final VoxelShape LINE_DOWN = VoxelShapes.or(LINE_DOWN_1, LINE_DOWN_2, LINE_DOWN_3);

    public ImporterBlock() {
        super(BlockUtils.DEFAULT_GLASS_PROPERTIES);
    }

    @Override
    public BlockDirection getDirection() {
        return BlockDirection.ANY;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return ShapeCache.getOrCreate(state, s -> {
            VoxelShape shape = getCableShape(s);

            shape = VoxelShapes.or(shape, getLineShape(s));

            return ConstantsCable.addCoverVoxelShapes(shape, world, pos);
        });
    }

    private VoxelShape getLineShape(BlockState state) {
        Direction direction = state.get(getDirection().getProperty());

        if (direction == Direction.NORTH) {
            return LINE_NORTH;
        }

        if (direction == Direction.EAST) {
            return LINE_EAST;
        }

        if (direction == Direction.SOUTH) {
            return LINE_SOUTH;
        }

        if (direction == Direction.WEST) {
            return LINE_WEST;
        }

        if (direction == Direction.UP) {
            return LINE_UP;
        }

        if (direction == Direction.DOWN) {
            return LINE_DOWN;
        }

        return VoxelShapes.empty();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ImporterTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote && CollisionUtils.isInBounds(getLineShape(state), pos, hit.getHitVec())) {
            return NetworkUtils.attemptModify(world, pos, player, () -> NetworkHooks.openGui(
                (ServerPlayerEntity) player,
                new PositionalTileContainerProvider<ImporterTile>(
                    new TranslationTextComponent("gui.refinedstorage.importer"),
                    (tile, windowId, inventory, p) -> new ImporterContainer(tile, player, windowId),
                    pos
                ),
                pos
            ));
        }

        return ActionResultType.SUCCESS;
    }
}
