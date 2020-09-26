package com.refinedmods.refinedstorage.apiimpl.autocrafting;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllowedTagList {
    private static final String NBT_ALLOWED_ITEM_TAGS = "AllowedItemTags";
    private static final String NBT_ALLOWED_FLUID_TAGS = "AllowedFluidTags";

    private List<Set<ResourceLocation>> allowedItemTags = new ArrayList<>();
    private List<Set<ResourceLocation>> allowedFluidTags = new ArrayList<>();

    @Nullable
    private final Runnable listener;

    public AllowedTagList(@Nullable Runnable listener, int size) {
        for (int i = 0; i < size; ++i) {
            allowedItemTags.add(new HashSet<>());
            allowedFluidTags.add(new HashSet<>());
        }

        this.listener = listener;
    }

    public CompoundNBT writeToNbt() {
        CompoundNBT tag = new CompoundNBT();

        tag.put(NBT_ALLOWED_ITEM_TAGS, getList(allowedItemTags));
        tag.put(NBT_ALLOWED_FLUID_TAGS, getList(allowedFluidTags));

        return tag;
    }

    public void readFromNbt(CompoundNBT tag) {
        if (tag.contains(NBT_ALLOWED_ITEM_TAGS)) {
            applyList(allowedItemTags, tag.getList(NBT_ALLOWED_ITEM_TAGS, Constants.NBT.TAG_LIST));
        }

        if (tag.contains(NBT_ALLOWED_FLUID_TAGS)) {
            applyList(allowedFluidTags, tag.getList(NBT_ALLOWED_FLUID_TAGS, Constants.NBT.TAG_LIST));
        }
    }

    private ListNBT getList(List<Set<ResourceLocation>> tagsPerSlot) {
        ListNBT list = new ListNBT();

        for (Set<ResourceLocation> tags : tagsPerSlot) {
            ListNBT subList = new ListNBT();

            tags.forEach(t -> subList.add(StringNBT.valueOf(t.toString())));

            list.add(subList);
        }

        return list;
    }

    private void applyList(List<Set<ResourceLocation>> list, ListNBT tagList) {
        for (int i = 0; i < tagList.size(); ++i) {
            ListNBT subList = tagList.getList(i);

            for (int j = 0; j < subList.size(); ++j) {
                list.get(i).add(new ResourceLocation(subList.getString(j)));
            }
        }
    }

    public List<Set<ResourceLocation>> getAllowedItemTags() {
        return allowedItemTags;
    }

    public List<Set<ResourceLocation>> getAllowedFluidTags() {
        return allowedFluidTags;
    }

    public void setAllowedItemTags(List<Set<ResourceLocation>> allowedItemTags) {
        this.allowedItemTags = allowedItemTags;

        notifyListener();
    }

    public void setAllowedFluidTags(List<Set<ResourceLocation>> allowedFluidTags) {
        this.allowedFluidTags = allowedFluidTags;

        notifyListener();
    }

    public void clearItemTags(int slot) {
        this.allowedItemTags.get(slot).clear();

        notifyListener();
    }

    public void clearFluidTags(int slot) {
        this.allowedFluidTags.get(slot).clear();

        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }
}
