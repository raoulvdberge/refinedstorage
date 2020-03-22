package com.raoulvdberge.refinedstorage.item.blockitem;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.disk.StorageDiskSyncData;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.storage.StorageNetworkNode;
import com.raoulvdberge.refinedstorage.apiimpl.storage.ItemStorageType;
import com.raoulvdberge.refinedstorage.block.StorageBlock;
import com.raoulvdberge.refinedstorage.item.StoragePartItem;
import com.raoulvdberge.refinedstorage.render.Styles;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class StorageBlockItem extends BaseBlockItem {
    private final ItemStorageType type;

    public StorageBlockItem(StorageBlock block) {
        super(block, new Item.Properties().group(RS.MAIN_GROUP));

        this.type = block.getType();

        this.setRegistryName(block.getRegistryName());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (isValid(stack)) {
            UUID id = getId(stack);

            API.instance().getStorageDiskSync().sendRequest(id);

            StorageDiskSyncData data = API.instance().getStorageDiskSync().getData(id);
            if (data != null) {
                if (data.getCapacity() == -1) {
                    tooltip.add(new TranslationTextComponent("misc.refinedstorage.storage.stored", API.instance().getQuantityFormatter().format(data.getStored())).setStyle(Styles.GRAY));
                } else {
                    tooltip.add(new TranslationTextComponent("misc.refinedstorage.storage.stored_capacity", API.instance().getQuantityFormatter().format(data.getStored()), API.instance().getQuantityFormatter().format(data.getCapacity())).setStyle(Styles.GRAY));
                }
            }

            if (flag.isAdvanced()) {
                tooltip.add(new StringTextComponent(id.toString()).setStyle(Styles.GRAY));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack storageStack = player.getHeldItem(hand);

        if (!world.isRemote && player.isCrouching() && type != ItemStorageType.CREATIVE) {
            UUID diskId = null;
            IStorageDisk disk = null;

            if (isValid(storageStack)) {
                diskId = getId(storageStack);
                disk = API.instance().getStorageDiskManager((ServerWorld) world).get(diskId);
            }

            // Newly created storages won't have a tag yet, so allow invalid disks as well.
            if (disk == null || disk.getStored() == 0) {
                ItemStack storagePart = new ItemStack(StoragePartItem.getByType(type));

                if (!player.inventory.addItemStackToInventory(storagePart.copy())) {
                    InventoryHelper.spawnItemStack(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), storagePart);
                }

                if (disk != null) {
                    API.instance().getStorageDiskManager((ServerWorld) world).remove(diskId);
                    API.instance().getStorageDiskManager((ServerWorld) world).markForSaving();
                }

                return new ActionResult<>(ActionResultType.SUCCESS, new ItemStack(RSBlocks.MACHINE_CASING));
            }
        }

        return new ActionResult<>(ActionResultType.PASS, storageStack);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    private UUID getId(ItemStack disk) {
        return disk.getTag().getUniqueId(StorageNetworkNode.NBT_ID);
    }

    private boolean isValid(ItemStack disk) {
        return disk.hasTag() && disk.getTag().hasUniqueId(StorageNetworkNode.NBT_ID);
    }
}
