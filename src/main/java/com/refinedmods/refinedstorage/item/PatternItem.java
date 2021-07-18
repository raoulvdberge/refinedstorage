package com.refinedmods.refinedstorage.item;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.RSItems;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.AllowedTagList;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.CraftingPattern;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.CraftingPatternFactory;
import com.refinedmods.refinedstorage.render.Styles;
import com.refinedmods.refinedstorage.render.tesr.PatternItemStackTileRenderer;
import com.refinedmods.refinedstorage.util.ItemStackKey;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PatternItem extends Item implements ICraftingPatternProvider {
    private static final Map<ItemStackKey, ICraftingPattern> CACHE = new HashMap<>();

    private static final String NBT_VERSION = "Version";
    private static final String NBT_INPUT_SLOT = "Input_%d";
    private static final String NBT_OUTPUT_SLOT = "Output_%d";
    private static final String NBT_FLUID_INPUT_SLOT = "FluidInput_%d";
    private static final String NBT_FLUID_OUTPUT_SLOT = "FluidOutput_%d";
    private static final String NBT_EXACT = "Exact";
    private static final String NBT_PROCESSING = "Processing";
    private static final String NBT_ALLOWED_TAGS = "AllowedTags";
    private static final String NBT_PATTERN_CREATOR = "PatternCreator";

    private static final int VERSION = 1;

    public PatternItem() {
        super(new Item.Properties().group(RS.MAIN_GROUP).setISTER(() -> PatternItemStackTileRenderer::new));
    }

    public static ICraftingPattern fromCache(World world, ItemStack stack) {
        ICraftingPattern pattern = CACHE.computeIfAbsent(
            new ItemStackKey(stack),
            s -> CraftingPatternFactory.INSTANCE.create(world, null, s.getStack())
        );

        // A number that is not too crazy but hopefully is not normally reachable,
        // just reset the cache to keep its size limited so this is not a memory leak
        if (CACHE.size() > 16384) {
            CACHE.clear();
        }

        return pattern;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (!stack.hasTag()) {
            return;
        }

        ICraftingPattern pattern = fromCache(world, stack);

        if (pattern.isValid()) {
            if (Screen.hasShiftDown() || isProcessing(stack)) {
                tooltip.add(new TranslationTextComponent("misc.refinedstorage.pattern.inputs").setStyle(Styles.YELLOW));

                RenderUtils.addCombinedItemsToTooltip(tooltip, true, pattern.getInputs().stream().map(i -> !i.isEmpty() ? i.get(0) : ItemStack.EMPTY).collect(Collectors.toList()));
                RenderUtils.addCombinedFluidsToTooltip(tooltip, true, pattern.getFluidInputs().stream().map(i -> !i.isEmpty() ? i.get(0) : FluidStack.EMPTY).collect(Collectors.toList()));

                tooltip.add(new TranslationTextComponent("misc.refinedstorage.pattern.outputs").setStyle(Styles.YELLOW));
            }

            RenderUtils.addCombinedItemsToTooltip(tooltip, true, pattern.getOutputs());
            RenderUtils.addCombinedFluidsToTooltip(tooltip, true, pattern.getFluidOutputs());

            if (pattern instanceof CraftingPattern && ((CraftingPattern) pattern).getAllowedTagList() != null) {
                addAllowedTags(tooltip, (CraftingPattern) pattern);
            }

            if (isExact(stack)) {
                tooltip.add(new TranslationTextComponent("misc.refinedstorage.pattern.exact").setStyle(Styles.BLUE));
            }

            if (isProcessing(stack)) {
                tooltip.add(new TranslationTextComponent("misc.refinedstorage.processing").setStyle(Styles.BLUE));
            }
        } else {
            tooltip.add(new TranslationTextComponent("misc.refinedstorage.pattern.invalid").setStyle(Styles.RED));
            tooltip.add(pattern.getErrorMessage().copyRaw().setStyle(Styles.GRAY));
        }
    }

    public void addAllowedTags(List<ITextComponent> tooltip, CraftingPattern pattern) {
        for (int i = 0; i < pattern.getAllowedTagList().getAllowedItemTags().size(); ++i) {
            Set<ResourceLocation> allowedTags = pattern.getAllowedTagList().getAllowedItemTags().get(i);

            for (ResourceLocation tag : allowedTags) {
                tooltip.add(new TranslationTextComponent(
                    "misc.refinedstorage.pattern.allowed_item_tag",
                    tag.toString(),
                    pattern.getInputs().get(i).get(0).getDisplayName()
                ).setStyle(Styles.AQUA));
            }
        }

        for (int i = 0; i < pattern.getAllowedTagList().getAllowedFluidTags().size(); ++i) {
            Set<ResourceLocation> allowedTags = pattern.getAllowedTagList().getAllowedFluidTags().get(i);

            for (ResourceLocation tag : allowedTags) {
                tooltip.add(new TranslationTextComponent(
                    "misc.refinedstorage.pattern.allowed_fluid_tag",
                    tag.toString(),
                    pattern.getFluidInputs().get(i).get(0).getDisplayName()
                ).setStyle(Styles.AQUA));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (!world.isRemote && player.isCrouching()) {
            return new ActionResult<>(ActionResultType.SUCCESS, new ItemStack(RSItems.PATTERN.get(), player.getHeldItem(hand).getCount()));
        }

        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    @Nonnull
    public ICraftingPattern create(World world, ItemStack stack, ICraftingPatternContainer container) {
        return CraftingPatternFactory.INSTANCE.create(world, container, stack);
    }

    public static void setInputSlot(ItemStack pattern, int slot, ItemStack stack) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().put(String.format(NBT_INPUT_SLOT, slot), stack.serializeNBT());
    }

    @Nonnull
    public static ItemStack getInputSlot(ItemStack pattern, int slot) {
        String id = String.format(NBT_INPUT_SLOT, slot);

        if (!pattern.hasTag() || !pattern.getTag().contains(id)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.read(pattern.getTag().getCompound(id));
    }

    public static void setOutputSlot(ItemStack pattern, int slot, ItemStack stack) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().put(String.format(NBT_OUTPUT_SLOT, slot), stack.serializeNBT());
    }

    @Nonnull
    public static ItemStack getOutputSlot(ItemStack pattern, int slot) {
        String id = String.format(NBT_OUTPUT_SLOT, slot);

        if (!pattern.hasTag() || !pattern.getTag().contains(id)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.read(pattern.getTag().getCompound(id));
    }

    public static void setFluidInputSlot(ItemStack pattern, int slot, FluidStack stack) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().put(String.format(NBT_FLUID_INPUT_SLOT, slot), stack.writeToNBT(new CompoundNBT()));
    }

    public static FluidStack getFluidInputSlot(ItemStack pattern, int slot) {
        String id = String.format(NBT_FLUID_INPUT_SLOT, slot);

        if (!pattern.hasTag() || !pattern.getTag().contains(id)) {
            return FluidStack.EMPTY;
        }

        return FluidStack.loadFluidStackFromNBT(pattern.getTag().getCompound(id));
    }

    public static void setFluidOutputSlot(ItemStack pattern, int slot, FluidStack stack) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().put(String.format(NBT_FLUID_OUTPUT_SLOT, slot), stack.writeToNBT(new CompoundNBT()));
    }

    public static FluidStack getFluidOutputSlot(ItemStack pattern, int slot) {
        String id = String.format(NBT_FLUID_OUTPUT_SLOT, slot);

        if (!pattern.hasTag() || !pattern.getTag().contains(id)) {
            return FluidStack.EMPTY;
        }

        return FluidStack.loadFluidStackFromNBT(pattern.getTag().getCompound(id));
    }

    public static boolean isProcessing(ItemStack pattern) {
        return pattern.hasTag() && pattern.getTag().contains(NBT_PROCESSING) && pattern.getTag().getBoolean(NBT_PROCESSING);
    }

    public static void setProcessing(ItemStack pattern, boolean processing) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().putBoolean(NBT_PROCESSING, processing);
    }

    public static boolean isExact(ItemStack pattern) {
        if (!pattern.hasTag() || !pattern.getTag().contains(NBT_EXACT)) {
            return false;
        }

        return pattern.getTag().getBoolean(NBT_EXACT);
    }

    public static void setExact(ItemStack pattern, boolean exact) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().putBoolean(NBT_EXACT, exact);
    }

    public static void setToCurrentVersion(ItemStack pattern) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().putInt(NBT_VERSION, VERSION);
    }

    public static void setAllowedTags(ItemStack pattern, AllowedTagList allowedTagList) {
        if (!pattern.hasTag()) {
            pattern.setTag(new CompoundNBT());
        }

        pattern.getTag().put(NBT_ALLOWED_TAGS, allowedTagList.writeToNbt());
    }

    @Nullable
    public static AllowedTagList getAllowedTags(ItemStack pattern) {
        if (!pattern.hasTag() || !pattern.getTag().contains(NBT_ALLOWED_TAGS)) {
            return null;
        }

        AllowedTagList allowedTagList = new AllowedTagList(null);

        allowedTagList.readFromNbt(pattern.getTag().getCompound(NBT_ALLOWED_TAGS));

        return allowedTagList;
    }
    
    @Nullable
    public static UUID getPatternCreator(ItemStack pattern) {
        if (!pattern.hasTag() || !pattern.getTag().contains(NBT_PATTERN_CREATOR)) {
            return null;
        }
        return pattern.getTag().getUniqueId(NBT_PATTERN_CREATOR);
    }
    
    public static void setPatternCreator(ItemStack pattern, UUID creator) {
        if (!pattern.hasTag()) {
                pattern.setTag(new CompoundNBT());
        }
        pattern.getTag().putUniqueId(NBT_PATTERN_CREATOR, creator);
    }
}
