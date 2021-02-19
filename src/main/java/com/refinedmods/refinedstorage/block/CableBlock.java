package com.refinedmods.refinedstorage.block;

import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.Cover;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverType;
import com.refinedmods.refinedstorage.block.shape.ShapeCache;
import com.refinedmods.refinedstorage.capability.NetworkNodeProxyCapability;
import com.refinedmods.refinedstorage.render.ConstantsCable;
import com.refinedmods.refinedstorage.tile.CableTile;
import com.refinedmods.refinedstorage.tile.NetworkNodeTile;
import com.refinedmods.refinedstorage.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class CableBlock extends NetworkNodeBlock implements IWaterLoggable {
    private static final BooleanProperty NORTH = BooleanProperty.create("north");
    private static final BooleanProperty EAST = BooleanProperty.create("east");
    private static final BooleanProperty SOUTH = BooleanProperty.create("south");
    private static final BooleanProperty WEST = BooleanProperty.create("west");
    private static final BooleanProperty UP = BooleanProperty.create("up");
    private static final BooleanProperty DOWN = BooleanProperty.create("down");
    private static final BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");

    protected static final VoxelShape HOLDER_NORTH = makeCuboidShape(7, 7, 2, 9, 9, 6);
    protected static final VoxelShape HOLDER_EAST = makeCuboidShape(10, 7, 7, 14, 9, 9);
    protected static final VoxelShape HOLDER_SOUTH = makeCuboidShape(7, 7, 10, 9, 9, 14);
    protected static final VoxelShape HOLDER_WEST = makeCuboidShape(2, 7, 7, 6, 9, 9);
    protected static final VoxelShape HOLDER_UP = makeCuboidShape(7, 10, 7, 9, 14, 9);
    protected static final VoxelShape HOLDER_DOWN = makeCuboidShape(7, 2, 7, 9, 6, 9);

    private static final VoxelShape SHAPE_CORE = makeCuboidShape(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = makeCuboidShape(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_EAST = makeCuboidShape(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_SOUTH = makeCuboidShape(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = makeCuboidShape(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_UP = makeCuboidShape(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = makeCuboidShape(6, 0, 6, 10, 6, 10);

    public CableBlock(Properties props) {
        super(props);
        this.setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    public CableBlock() {
        super(BlockUtils.DEFAULT_GLASS_PROPERTIES);

        this.setDefaultState(getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(UP, false).with(DOWN, false).with(WATERLOGGED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        return getState(state, world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return ConstantsCable.addCoverVoxelShapes(ShapeCache.getOrCreate(state, CableBlock::getCableShape), world, pos);
    }

    protected static VoxelShape getCableShape(BlockState state) {
        VoxelShape shape = SHAPE_CORE;

        if (Boolean.TRUE.equals(state.get(NORTH))) {
            shape = VoxelShapes.or(shape, SHAPE_NORTH);
        }

        if (Boolean.TRUE.equals(state.get(EAST))) {
            shape = VoxelShapes.or(shape, SHAPE_EAST);
        }

        if (Boolean.TRUE.equals(state.get(SOUTH))) {
            shape = VoxelShapes.or(shape, SHAPE_SOUTH);
        }

        if (Boolean.TRUE.equals(state.get(WEST))) {
            shape = VoxelShapes.or(shape, SHAPE_WEST);
        }

        if (Boolean.TRUE.equals(state.get(UP))) {
            shape = VoxelShapes.or(shape, SHAPE_UP);
        }

        if (Boolean.TRUE.equals(state.get(DOWN))) {
            shape = VoxelShapes.or(shape, SHAPE_DOWN);
        }

        return shape;
    }

    @Override
    protected void onDirectionChanged(World world, BlockPos pos, Direction newDirection) {
        // rotate() in BaseBlock "stupidly" changes the direction without checking if our cable connections are still valid.
        // You'd expect that cable connections are not changing when simply changing the direction.
        // But they need to. For example, when rotating a constructor to connect to a neighboring cable, the connection to that neighbor
        // needs to be removed as otherwise the "holder" of the constructor will conflict with the cable connection.
        // This is already checked in hasNode().
        // But since rotate() doesn't invalidate that connection, we need to do it here.
        // Ideally, this code would be in rotate(). But rotate() doesn't have any data about the position and world, so we need to do it here.
        world.setBlockState(pos, getState(world.getBlockState(pos), world, pos));

        super.onDirectionChanged(world, pos, newDirection);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getState(getDefaultState(), ctx.getWorld(), ctx.getPos());
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return Boolean.TRUE.equals(state.get(WATERLOGGED)) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        return IWaterLoggable.super.receiveFluid(worldIn, pos, state, fluidStateIn);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return IWaterLoggable.super.canContainFluid(worldIn, pos, state, fluidIn);
    }

    private boolean hasNode(IWorld world, BlockPos pos, BlockState state, Direction direction) {
        // Prevent the "holder" of a cable block conflicting with a cable connection.
        if (getDirection() != BlockDirection.NONE && state.get(getDirection().getProperty()).getOpposite() == direction) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }

        return tile.getCapability(NetworkNodeProxyCapability.NETWORK_NODE_PROXY_CAPABILITY, direction).isPresent();
    }

    private BlockState getState(BlockState currentState, IWorld world, BlockPos pos) {
        boolean north = hasNode(world, pos.offset(Direction.NORTH), currentState, Direction.SOUTH);
        boolean east = hasNode(world, pos.offset(Direction.EAST), currentState, Direction.WEST);
        boolean south = hasNode(world, pos.offset(Direction.SOUTH), currentState, Direction.NORTH);
        boolean west = hasNode(world, pos.offset(Direction.WEST), currentState, Direction.EAST);
        boolean up = hasNode(world, pos.offset(Direction.UP), currentState, Direction.DOWN);
        boolean down = hasNode(world, pos.offset(Direction.DOWN), currentState, Direction.UP);

        return currentState
            .with(NORTH, north)
            .with(EAST, east)
            .with(SOUTH, south)
            .with(WEST, west)
            .with(UP, up)
            .with(DOWN, down);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CableTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }
}
