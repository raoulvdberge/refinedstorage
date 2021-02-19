package com.refinedmods.refinedstorage.render.model;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.RSBlocks;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.Cover;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.block.BaseBlock;
import com.refinedmods.refinedstorage.render.ConstantsCable;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BakedModelCableCover extends DelegateBakedModel{

    private static TextureAtlasSprite BORDER_SPRITE;

    public BakedModelCableCover(IBakedModel base) {
        super(base);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        List<BakedQuad> quads = new ArrayList<>(base.getQuads(state, side, rand, data));
        if (data != null && data.hasProperty(CoverManager.PROPERTY)) {
            CoverManager manager = data.getData(CoverManager.PROPERTY);
            addCover(quads, manager.getCover(Direction.NORTH), Direction.NORTH, side, rand, manager, state, true);
            addCover(quads, manager.getCover(Direction.SOUTH), Direction.SOUTH, side, rand, manager, state, true);
            addCover(quads, manager.getCover(Direction.EAST), Direction.EAST, side, rand, manager, state, true);
            addCover(quads, manager.getCover(Direction.WEST), Direction.WEST, side, rand, manager, state, true);
            addCover(quads, manager.getCover(Direction.DOWN), Direction.DOWN, side, rand, manager, state, true);
            addCover(quads, manager.getCover(Direction.UP), Direction.UP, side, rand, manager, state, true);
        }
        return quads;
    }

    private static int getHollowCoverSize(@Nullable BlockState state, Direction coverSide) {
        if (state == null) {
            return 6;
        }

        BaseBlock block = (BaseBlock) state.getBlock();
        if (block == RSBlocks.CABLE.get()){
            return 6;
        }

        if (block.getDirection() != null && state.get(block.getDirection().getProperty()) == coverSide) {
            if (block == RSBlocks.EXPORTER.get()) {
                return 6;
            } else if (block == RSBlocks.EXTERNAL_STORAGE.get() || block == RSBlocks.IMPORTER.get()) {
                return 3;
            } else if (block == RSBlocks.CONSTRUCTOR.get() || block == RSBlocks.DESTRUCTOR.get()) { //Removed reader and writer
                return 2;
            }
        }

        return 6;
    }

    protected static void addCover(List<BakedQuad> quads, @Nullable Cover cover, Direction coverSide, Direction side, Random rand, @Nullable CoverManager manager, BlockState state,  boolean handle) {
        if (cover == null) {
            return;
        }

        BlockState coverState = CoverManager.getBlockState(cover.getStack());

        if (coverState == null) {
            return;
        }

        boolean hasUp = false, hasDown = false, hasEast = false, hasWest = false;

        if (manager != null) {
            hasUp = manager.hasCover(Direction.UP);
            hasDown = manager.hasCover(Direction.DOWN);
            hasEast = manager.hasCover(Direction.EAST);
            hasWest = manager.hasCover(Direction.WEST);
        }

        TextureAtlasSprite sprite = RenderUtils.getSprite(Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(coverState), coverState, side, rand);

        switch (cover.getType()) {
            case NORMAL:
                addNormalCover(quads, sprite, coverSide, hasUp, hasDown, hasEast, hasWest, handle);
                break;
            case HOLLOW:
                addHollowCover(quads, sprite, coverSide, hasUp, hasDown, hasEast, hasWest, getHollowCoverSize(state, coverSide));
                break;
        }
    }

    private static void addNormalCover(List<BakedQuad> quads, TextureAtlasSprite sprite, Direction coverSide, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest, boolean handle) {
        AxisAlignedBB bounds = ConstantsCable.getCoverBounds(coverSide);

        Vector3f from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
        Vector3f to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

        if (coverSide == Direction.NORTH) {
            if (hasWest) {
                from.setX(2);
            }

            if (hasEast) {
                to.setX(14);
            }
        } else if (coverSide == Direction.SOUTH) {
            if (hasWest) {
                from.setX(2);
            }

            if (hasEast) {
                to.setX(14);
            }
        }

        if (coverSide.getAxis() != Direction.Axis.Y) {
            if (hasDown) {
                from.setY(2);
            }

            if (hasUp) {
                to.setY(14);
            }
        }

        quads.addAll(new CubeBuilder().from(from.getX(), from.getY(), from.getZ()).to(to.getX(), to.getY(), to.getZ()).addFaces(face -> new CubeBuilder.Face(face, sprite)).bake());

        if (handle) {
            if (BORDER_SPRITE == null) {
                BORDER_SPRITE = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation(RS.ID , "block/cable_part_border"));
            }

            bounds = ConstantsCable.getHolderBounds(coverSide);

            from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
            to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

            quads.addAll(new CubeBuilder().from(from.getX(), from.getY(), from.getZ()).to(to.getX(), to.getY(), to.getZ()).addFaces(face -> new CubeBuilder.Face(face, BORDER_SPRITE)).bake());
        }
    }

    private static void addHollowCover(List<BakedQuad> quads, TextureAtlasSprite sprite, Direction coverSide, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest, int size) {
        AxisAlignedBB bounds = ConstantsCable.getCoverBounds(coverSide);

        Vector3f from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
        Vector3f to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

        if (coverSide.getAxis() != Direction.Axis.Y) {
            if (hasDown) {
                from.setY(2);
            }

            if (hasUp) {
                to.setY(14);
            }
        }

        // Right
        if (coverSide == Direction.NORTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(size);
        } else if (coverSide == Direction.SOUTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(16 - size);
        } else if (coverSide == Direction.EAST) {
            from.setZ(0);
            to.setZ(size);
        } else if (coverSide == Direction.WEST) {
            from.setZ(16 - size);
            to.setZ(16);
        } else if (coverSide == Direction.DOWN || coverSide == Direction.UP) {
            from.setZ(16 - size);
            to.setZ(16);
        }

        quads.addAll(new CubeBuilder()
                .from(from.getX(), from.getY(), from.getZ())
                .to(to.getX(), to.getY(), to.getZ())
                .addFaces(face -> new CubeBuilder.Face(face, sprite))
                .bake()
        );

        // Left
        if (coverSide == Direction.NORTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(16 - size);
        } else if (coverSide == Direction.SOUTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(size);
        } else if (coverSide == Direction.EAST) {
            from.setZ(16 - size);
            to.setZ(16);
        } else if (coverSide == Direction.WEST) {
            from.setZ(0);
            to.setZ(size);
        } else if (coverSide == Direction.DOWN || coverSide == Direction.UP) {
            from.setZ(0);
            to.setZ(size);
        }

        quads.addAll(new CubeBuilder()
                .from(from.getX(), from.getY(), from.getZ())
                .to(to.getX(), to.getY(), to.getZ())
                .addFaces(face -> new CubeBuilder.Face(face, sprite))
                .bake()
        );

        // Bottom
        if (coverSide == Direction.NORTH) {
            from.setX(size);
            to.setX(16 - size);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(size);
        } else if (coverSide == Direction.SOUTH) {
            from.setX(size);
            to.setX(16 - size);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(size);
        } else if (coverSide == Direction.EAST) {
            from.setZ(size);
            to.setZ(16 - size);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(size);
        } else if (coverSide == Direction.WEST) {
            from.setZ(size);
            to.setZ(16 - size);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(size);
        } else if (coverSide == Direction.DOWN || coverSide == Direction.UP) {
            from.setZ(size);
            to.setZ(16 - size);

            from.setX(0);
            to.setX(size);
        }

        quads.addAll(new CubeBuilder()
                .from(from.getX(), from.getY(), from.getZ())
                .to(to.getX(), to.getY(), to.getZ())
                .addFaces(face -> new CubeBuilder.Face(face, sprite))
                .bake()
        );

        // Up
        if (coverSide == Direction.NORTH) {
            from.setX(size);
            to.setX(16 - size);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(16 - size);
        } else if (coverSide == Direction.SOUTH) {
            from.setX(size);
            to.setX(16 - size);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(16 - size);
        } else if (coverSide == Direction.EAST) {
            from.setZ(size);
            to.setZ(16 - size);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(16 - size);
        } else if (coverSide == Direction.WEST) {
            from.setZ(size);
            to.setZ(16 - size);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(16 - size);
        } else if (coverSide == Direction.DOWN || coverSide == Direction.UP) {
            from.setZ(size);
            to.setZ(16 - size);

            from.setX(16 - size);
            to.setX(16);
        }

        quads.addAll(new CubeBuilder()
                .from(from.getX(), from.getY(), from.getZ())
                .to(to.getX(), to.getY(), to.getZ())
                .addFaces(face -> new CubeBuilder.Face(face, sprite))
                .bake()
        );
    }
}

