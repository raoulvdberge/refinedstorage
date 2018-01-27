package com.raoulvdberge.refinedstorage.apiimpl.network.node;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.externalstorage.StorageItemItemHandler;
import com.raoulvdberge.refinedstorage.inventory.*;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class NetworkNodeInterface extends NetworkNode implements IComparable {
    public static final String ID = "interface";

    private static final String NBT_COMPARE = "Compare";

    private ItemHandlerBase importItems = new ItemHandlerBase(9, new ItemHandlerListenerNetworkNode(this));

    private ItemHandlerBase exportSpecimenItems = new ItemHandlerBase(9, new ItemHandlerListenerNetworkNode(this)) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (network != null) {
                if (!isEmpty() && itemsStorage != null) {
                    removeItemStorage(network);
                } else if (isEmpty() && itemsStorage == null) {
                    createItemStorage(network);
                }
            }
        }
    };
    private ItemHandlerBase exportItems = new ItemHandlerBase(9, new ItemHandlerListenerNetworkNode(this));

    private IItemHandler items = new ItemHandlerProxy(importItems, exportItems);
    private ItemHandlerInterface itemsStorage;

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, new ItemHandlerListenerNetworkNode(this), ItemUpgrade.TYPE_SPEED, ItemUpgrade.TYPE_STACK, ItemUpgrade.TYPE_CRAFTING);

    private int compare = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;

    private int currentSlot = 0;

    public NetworkNodeInterface(World world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.interfaceUsage + upgrades.getEnergyUsage();
    }

    @Override
    public void update() {
        super.update();

        if (network == null || !canUpdate()) {
            return;
        }

        if (currentSlot >= importItems.getSlots()) {
            currentSlot = 0;
        }

        ItemStack slot = importItems.getStackInSlot(currentSlot);

        if (slot.isEmpty()) {
            currentSlot++;
        } else if (ticks % upgrades.getSpeed() == 0) {
            int size = Math.min(slot.getCount(), upgrades.getItemInteractCount());

            ItemStack remainder = network.insertItemTracked(slot, size);

            if (remainder == null) {
                importItems.extractItem(currentSlot, size, false);
            } else if (size - remainder.getCount() > 0) {
                importItems.extractItem(currentSlot, size - remainder.getCount(), false);

                currentSlot++;
            }
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack wanted = exportSpecimenItems.getStackInSlot(i);
            ItemStack got = exportItems.getStackInSlot(i);

            if (wanted.isEmpty()) {
                if (!got.isEmpty()) {
                    exportItems.setStackInSlot(i, StackUtils.nullToEmpty(network.insertItemTracked(got, got.getCount())));
                }
            } else if (!got.isEmpty() && !API.instance().getComparer().isEqual(wanted, got, getCompare())) {
                exportItems.setStackInSlot(i, StackUtils.nullToEmpty(network.insertItemTracked(got, got.getCount())));
            } else {
                int delta = got.isEmpty() ? wanted.getCount() : (wanted.getCount() - got.getCount());

                if (delta > 0) {
                    ItemStack result = network.extractItem(wanted, delta, compare, false, s -> !(s instanceof StorageItemItemHandler) || !((StorageItemItemHandler) s).isConnectedToInterface());

                    if (result != null) {
                        if (exportItems.getStackInSlot(i).isEmpty()) {
                            exportItems.setStackInSlot(i, result);
                        } else {
                            exportItems.getStackInSlot(i).grow(result.getCount());
                        }
                    }

                    // Example: our delta is 5, we extracted 3 items.
                    // That means we still have to autocraft 2 items.
                    delta -= result == null ? 0 : result.getCount();

                    if (delta > 0 && upgrades.hasUpgrade(ItemUpgrade.TYPE_CRAFTING)) {
                        network.getCraftingManager().schedule(wanted, delta, compare);
                    }
                } else if (delta < 0) {
                    ItemStack remainder = network.insertItemTracked(got, Math.abs(delta));

                    if (remainder == null) {
                        exportItems.extractItem(i, Math.abs(delta), false);
                    } else {
                        exportItems.extractItem(i, Math.abs(delta) - remainder.getCount(), false);
                    }
                }
            }
        }
    }

    @Override
    protected void onConnectedStateChange(INetwork network, boolean state) {
        super.onConnectedStateChange(network, state);

        if (state && exportSpecimenItems.isEmpty()) {
            createItemStorage(network);
        } else if (itemsStorage != null) {
            removeItemStorage(network);
        }
    }

    private void createItemStorage(INetwork network) {
        itemsStorage = new ItemHandlerInterface(network, network.getItemStorageCache(), importItems);

        network.getItemStorageCache().addListener(itemsStorage);
    }

    private void removeItemStorage(INetwork network) {
        network.getItemStorageCache().removeListener(itemsStorage);

        itemsStorage = null;
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;

        markDirty();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        StackUtils.readItems(importItems, 0, tag);
        StackUtils.readItems(exportItems, 2, tag);
        StackUtils.readItems(upgrades, 3, tag);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        StackUtils.writeItems(importItems, 0, tag);
        StackUtils.writeItems(exportItems, 2, tag);
        StackUtils.writeItems(upgrades, 3, tag);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        StackUtils.writeItems(exportSpecimenItems, 1, tag);

        tag.setInteger(NBT_COMPARE, compare);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        StackUtils.readItems(exportSpecimenItems, 1, tag);

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }
    }

    public IItemHandler getImportItems() {
        return importItems;
    }

    public IItemHandler getExportSpecimenItems() {
        return exportSpecimenItems;
    }

    public IItemHandler getExportItems() {
        return exportItems;
    }

    public IItemHandler getItemsOrStorage() {
        return itemsStorage != null ? itemsStorage : items;
    }

    public IItemHandler getItems() {
        return items;
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public IItemHandler getDrops() {
        return new CombinedInvWrapper(importItems, exportItems, upgrades);
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }
}
