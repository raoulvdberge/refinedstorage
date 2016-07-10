package refinedstorage.tile;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import refinedstorage.RefinedStorage;
import refinedstorage.RefinedStorageItems;
import refinedstorage.RefinedStorageUtils;
import refinedstorage.api.network.IWirelessTransmitter;
import refinedstorage.container.ContainerWirelessTransmitter;
import refinedstorage.inventory.BasicItemHandler;
import refinedstorage.inventory.BasicItemValidator;
import refinedstorage.item.ItemUpgrade;

public class TileWirelessTransmitter extends TileNode implements IWirelessTransmitter {
    private BasicItemHandler upgrades = new BasicItemHandler(4, this, new BasicItemValidator(RefinedStorageItems.UPGRADE, ItemUpgrade.TYPE_RANGE));

    @Override
    public int getEnergyUsage() {
        return RefinedStorage.INSTANCE.wirelessTransmitterUsage + RefinedStorageUtils.getUpgradeEnergyUsage(upgrades);
    }

    @Override
    public void updateNode() {
    }

    @Override
    public void read(NBTTagCompound nbt) {
        super.read(nbt);

        RefinedStorageUtils.readItems(upgrades, 0, nbt);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RefinedStorageUtils.writeItems(upgrades, 0, tag);

        return tag;
    }

    @Override
    public int getRange() {
        return RefinedStorage.INSTANCE.wirelessTransmitterBaseRange + (RefinedStorageUtils.getUpgradeCount(upgrades, ItemUpgrade.TYPE_RANGE) * RefinedStorage.INSTANCE.wirelessTransmitterRangePerUpgrade);
    }

    @Override
    public BlockPos getOrigin() {
        return pos;
    }

    public BasicItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerWirelessTransmitter.class;
    }

    @Override
    public IItemHandler getDroppedItems() {
        return upgrades;
    }

    @Override
    public boolean canConduct() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) upgrades;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
}
