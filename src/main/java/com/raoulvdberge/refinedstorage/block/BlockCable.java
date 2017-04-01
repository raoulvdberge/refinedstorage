package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.tile.*;
import mcmultipart.block.BlockCoverable;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockCable extends BlockCoverable {
    protected static final PropertyDirection DIRECTION = PropertyDirection.create("direction");

    protected static AxisAlignedBB createAABB(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return new AxisAlignedBB((float) fromX / 16F, (float) fromY / 16F, (float) fromZ / 16F, (float) toX / 16F, (float) toY / 16F, (float) toZ / 16F);
    }

    protected static AxisAlignedBB CORE_AABB = createAABB(6, 6, 6, 10, 10, 10);
    protected static AxisAlignedBB NORTH_AABB = createAABB(6, 6, 0, 10, 10, 6);
    protected static AxisAlignedBB EAST_AABB = createAABB(10, 6, 6, 16, 10, 10);
    protected static AxisAlignedBB SOUTH_AABB = createAABB(6, 6, 10, 10, 10, 16);
    protected static AxisAlignedBB WEST_AABB = createAABB(0, 6, 6, 6, 10, 10);
    protected static AxisAlignedBB UP_AABB = createAABB(6, 10, 6, 10, 16, 10);
    protected static AxisAlignedBB DOWN_AABB = createAABB(6, 0, 6, 10, 6, 10);

    protected static final PropertyBool NORTH = PropertyBool.create("north");
    protected static final PropertyBool EAST = PropertyBool.create("east");
    protected static final PropertyBool SOUTH = PropertyBool.create("south");
    protected static final PropertyBool WEST = PropertyBool.create("west");
    protected static final PropertyBool UP = PropertyBool.create("up");
    protected static final PropertyBool DOWN = PropertyBool.create("down");
    protected static final PropertyBool CONNECTED = PropertyBool.create("connected");

    private String name;

    public BlockCable(String name) {
        super(Material.ROCK);

        this.name = name;

        setHardness(0.6F);
        setRegistryName(RS.ID, name);
        setCreativeTab(RS.INSTANCE.tab);
    }

    public BlockCable() {
        this("cable");
    }

    @Override
    public String getUnlocalizedName() {
        return "block." + RS.ID + ":" + name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCable();
    }

    public boolean hasConnectivityState() {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);

        builder.add(NORTH)
            .add(EAST)
            .add(SOUTH)
            .add(WEST)
            .add(UP)
            .add(DOWN)
            .add(BlockMultipartContainer.PROPERTY_MULTIPART_CONTAINER);

        if (getPlacementType() != null) {
            builder.add(DIRECTION);
        }

        if (hasConnectivityState()) {
            builder.add(CONNECTED);
        }

        return builder.build();
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getActualState(state, world, pos)
            .withProperty(NORTH, hasConnectionWith(world, pos, EnumFacing.NORTH))
            .withProperty(EAST, hasConnectionWith(world, pos, EnumFacing.EAST))
            .withProperty(SOUTH, hasConnectionWith(world, pos, EnumFacing.SOUTH))
            .withProperty(WEST, hasConnectionWith(world, pos, EnumFacing.WEST))
            .withProperty(UP, hasConnectionWith(world, pos, EnumFacing.UP))
            .withProperty(DOWN, hasConnectionWith(world, pos, EnumFacing.DOWN));

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileNode) {
            if (getPlacementType() != null) {
                state = state.withProperty(DIRECTION, ((TileNode) tile).getDirection());
            }

            if (hasConnectivityState()) {
                state = state.withProperty(CONNECTED, ((TileNode) tile).isConnected());
            }
        }

        return state;
    }

    private boolean hasConnectionWith(IBlockAccess world, BlockPos pos, EnumFacing direction) {
        TileEntity facing = world.getTileEntity(pos.offset(direction));

        boolean isConnectable = API.instance().getConnectableConditions().stream().anyMatch(p -> p.test(facing));
        if (isConnectable) {
            TileEntity tile = world.getTileEntity(pos);

            // Do not render a cable extension where our cable "head" is (e.g. importer, exporter, external storage heads).
            if (tile instanceof TileMultipartNode) {
                TileMultipartNode multipartNode = (TileMultipartNode) tile;

                if (getPlacementType() != null && multipartNode != null && multipartNode.getFacingTile() == facing) {
                    return false;
                }

                return !TileMultipartNode.hasBlockingMicroblock(world, pos, direction) && !TileMultipartNode.hasBlockingMicroblock(world, pos.offset(direction), direction.getOpposite());
            }

            return true;
        }

        return false;
    }

    private boolean isInAABB(AxisAlignedBB aabb, float hitX, float hitY, float hitZ) {
        return hitX >= aabb.minX && hitX <= aabb.maxX && hitY >= aabb.minY && hitY <= aabb.maxY && hitZ >= aabb.minZ && hitZ <= aabb.maxZ;
    }

    protected boolean hitCablePart(IBlockState state, World world, BlockPos pos, float hitX, float hitY, float hitZ) {
        state = getActualState(state, world, pos);

        return isInAABB(CORE_AABB, hitX, hitY, hitZ) ||
            (state.getValue(NORTH) && isInAABB(NORTH_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(EAST) && isInAABB(EAST_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(SOUTH) && isInAABB(SOUTH_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(WEST) && isInAABB(WEST_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(UP) && isInAABB(UP_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(DOWN) && isInAABB(DOWN_AABB, hitX, hitY, hitZ));
    }

    public List<AxisAlignedBB> getUnionizedCollisionBoxes(IBlockState state) {
        List<AxisAlignedBB> boxes = new ArrayList<>();

        boxes.add(CORE_AABB);

        if (state.getValue(NORTH)) {
            boxes.add(NORTH_AABB);
        }

        if (state.getValue(EAST)) {
            boxes.add(EAST_AABB);
        }

        if (state.getValue(SOUTH)) {
            boxes.add(SOUTH_AABB);
        }

        if (state.getValue(WEST)) {
            boxes.add(WEST_AABB);
        }

        if (state.getValue(UP)) {
            boxes.add(UP_AABB);
        }

        if (state.getValue(DOWN)) {
            boxes.add(DOWN_AABB);
        }

        return boxes;
    }

    public List<AxisAlignedBB> getNonUnionizedCollisionBoxes(IBlockState state) {
        return Collections.emptyList();
    }

    public List<AxisAlignedBB> getCollisionBoxes(IBlockState state) {
        List<AxisAlignedBB> boxes = new ArrayList<>();

        boxes.addAll(getUnionizedCollisionBoxes(state));
        boxes.addAll(getNonUnionizedCollisionBoxes(state));

        return boxes;
    }

    @Override
    public void addCollisionBoxToListDefault(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
        for (AxisAlignedBB aabb : getCollisionBoxes(this.getActualState(state, world, pos))) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
        }
    }

    @Override
    public RayTraceResult collisionRayTraceDefault(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceUtils.AdvancedRayTraceResult result = RayTraceUtils.collisionRayTrace(world, pos, start, end, getCollisionBoxes(this.getActualState(state, world, pos)));

        return result != null ? result.hit : null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public EnumPlacementType getPlacementType() {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, entity);

        if (getPlacementType() != null) {
            return state.withProperty(DIRECTION, getPlacementType().getFrom(facing, pos, entity));
        }

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, player, stack);

        if (getPlacementType() != null) {
            ((TileBase) world.getTileEntity(pos)).setDirection(state.getValue(DIRECTION));
        }

        attemptConnect(world, pos);
    }

    public void attemptConnect(World world, BlockPos pos) {
        if (!world.isRemote) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity tile = world.getTileEntity(pos.offset(facing));

                if (tile instanceof INetworkNode && ((INetworkNode) tile).isConnected()) {
                    ((INetworkNode) tile).getNetwork().getNodeGraph().rebuild();

                    break;
                } else if (tile instanceof INetworkMaster) {
                    ((INetworkMaster) tile).getNodeGraph().rebuild();

                    break;
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        INetworkMaster network = null;

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileNode) {
                network = ((TileNode) tile).getNetwork();
            }

            if (tile instanceof TileBase && ((TileBase) tile).getDrops() != null) {
                IItemHandler handler = ((TileBase) tile).getDrops();

                for (int i = 0; i < handler.getSlots(); ++i) {
                    if (handler.getStackInSlot(i) != null) {
                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                    }
                }
            }
        }

        super.breakBlock(world, pos, state);

        if (network != null) {
            network.getNodeGraph().rebuild();
        }
    }

    @Override
    public List<ItemStack> getDropsDefault(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> drops = new ArrayList<>();

        drops.add(new ItemStack(this, 1, getMetaFromState(state)));

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileBase && ((TileBase) tile).getDrops() != null) {
            IItemHandler handler = ((TileBase) tile).getDrops();

            for (int i = 0; i < handler.getSlots(); ++i) {
                if (handler.getStackInSlot(i) != null) {
                    drops.add(handler.getStackInSlot(i));
                }
            }
        }

        return drops;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        if (!world.isRemote && getPlacementType() != null) {
            TileBase tile = (TileBase) world.getTileEntity(pos);

            tile.setDirection(getPlacementType().cycle(tile.getDirection()));

            tile.updateBlock();

            return true;
        }

        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
