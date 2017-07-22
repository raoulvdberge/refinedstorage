package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskProvider;
import com.raoulvdberge.refinedstorage.api.storage.StorageDiskType;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageDiskItem;
import com.raoulvdberge.refinedstorage.block.ItemStorageType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class ItemStorageDisk extends ItemBase implements IStorageDiskProvider<ItemStack> {
    public static final int TYPE_1K = 0;
    public static final int TYPE_4K = 1;
    public static final int TYPE_16K = 2;
    public static final int TYPE_64K = 3;
    public static final int TYPE_CREATIVE = 4;
    public static final int TYPE_DEBUG = 5;

    public ItemStorageDisk() {
        super("storage_disk");

        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }

        for (int i = 0; i < 5; ++i) {
            items.add(API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.ITEMS, new ItemStack(this, 1, i)));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.onUpdate(stack, world, entity, slot, selected);

        if (!stack.hasTagCompound()) {
            if (stack.getItemDamage() == TYPE_DEBUG) {
                applyDebugDiskData(stack);
            } else {
                API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.ITEMS, stack);
            }
        }
    }

    private void applyDebugDiskData(ItemStack stack) {
        NBTTagCompound debugDiskTag = API.instance().getDefaultStorageDiskBehavior().getTag(StorageDiskType.ITEMS);

        StorageDiskItem storage = new StorageDiskItem(debugDiskTag, -1);

        Iterator<Item> it = REGISTRY.iterator();

        while (it.hasNext()) {
            Item item = it.next();

            if (item != RSItems.STORAGE_DISK) {
                NonNullList<ItemStack> stacks = NonNullList.create();

                item.getSubItems(CreativeTabs.SEARCH, stacks);

                for (ItemStack itemStack : stacks) {
                    storage.insert(itemStack, 1000, false);
                }
            }
        }

        storage.writeToNBT();

        stack.setTagCompound(debugDiskTag);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        IStorageDisk storage = create(stack);

        if (storage.isValid(stack)) {
            if (storage.getCapacity() == -1) {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored", storage.getStored()));
            } else {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored_capacity", storage.getStored(), storage.getCapacity()));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack disk = player.getHeldItem(hand);

        IStorageDisk storage = create(disk);

        if (!world.isRemote && player.isSneaking() && storage.isValid(disk) && storage.getStored() <= 0 && disk.getMetadata() != TYPE_CREATIVE) {
            ItemStack storagePart = new ItemStack(RSItems.STORAGE_PART, 1, disk.getMetadata());

            if (!player.inventory.addItemStackToInventory(storagePart.copy())) {
                InventoryHelper.spawnItemStack(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), storagePart);
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(RSItems.STORAGE_HOUSING));
        }

        return new ActionResult<>(EnumActionResult.PASS, disk);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);

        API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.ITEMS, stack);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        return API.instance().getDefaultStorageDiskBehavior().getShareTag(StorageDiskType.ITEMS, stack);
    }

    @Nonnull
    @Override
    public IStorageDisk<ItemStack> create(ItemStack disk) {
        return API.instance().getDefaultStorageDiskBehavior().createItemStorage(disk.getTagCompound(), ItemStorageType.getById(disk.getItemDamage()).getCapacity());
    }
}
