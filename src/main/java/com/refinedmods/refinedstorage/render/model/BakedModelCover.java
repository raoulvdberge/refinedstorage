package com.refinedmods.refinedstorage.render.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.Cover;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverType;
import com.refinedmods.refinedstorage.item.CoverItem;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;

import javax.annotation.Nullable;
import java.util.*;

public class BakedModelCover extends BakedModelCableCover{

    private class CacheKey {
        private BlockState state;
        private ItemStack stack;
        private Direction side;
        private CoverType type;

        CacheKey(BlockState state, ItemStack stack, Direction side, CoverType type) {
            this.state = state;
            this.stack = stack;
            this.side = side;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BakedModelCover.CacheKey cacheKey = (BakedModelCover.CacheKey) o;

            return cacheKey.type == type && cacheKey.stack.getItem() == stack.getItem() /* && cacheKey.stack.getItemDamage() == stack.getItemDamage()*/ && cacheKey.side == side && Objects.equals(cacheKey.state, state);
        }

        @Override
        public int hashCode() {
            int result = stack.getItem().hashCode();
            //result = 31 * result + stack.getDamage();
            result = 31 * result + (side != null ? side.hashCode() : 0);
            result = 31 * result + (state != null ? state.hashCode() : 0);
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    private static final LoadingCache<CacheKey, List<BakedQuad>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, List<BakedQuad>>() {
        @Override
        public List<BakedQuad> load(CacheKey key) {
            List<BakedQuad> quads = new ArrayList<>();

            addCover(quads, new Cover(key.stack, key.type), Direction.NORTH, key.side, new Random(), null, null, true);

            return quads;
        }
    });

    private ItemStack stack;
    private CoverType type;

    public BakedModelCover(ItemStack stack, CoverType type) {
        super(null);

        this.stack = stack;
        this.type = type;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        CacheKey key = new CacheKey(state, CoverItem.getItem(stack), side, type);

        return CACHE.getUnchecked(key);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList() {
            @Override
            public IBakedModel func_239290_a_(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
                return new BakedModelCover(stack, type);
            }
        };
    }

    //    @Override
    //    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    //        TRSRTransformation transform = RenderUtils.getDefaultBlockTransforms().get(cameraTransformType);
    //
    //        return Pair.of(this, transform == null ? RenderUtils.EMPTY_MATRIX_TRANSFORM : transform.getMatrix());
    //    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack matrixStack) {
        TransformationMatrix transform = RenderUtils.getDefaultBlockTransforms().get(cameraTransformType);
        if (transform != null) transform.push(matrixStack);
        return this;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public boolean func_230044_c_() {
        return true;
    }
}
