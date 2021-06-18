package com.refinedmods.refinedstorage.apiimpl.network.node;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.inventory.item.BaseItemHandler;
import com.refinedmods.refinedstorage.inventory.item.UpgradeItemHandler;
import com.refinedmods.refinedstorage.inventory.item.validator.PatternItemValidator;
import com.refinedmods.refinedstorage.inventory.listener.NetworkNodeInventoryListener;
import com.refinedmods.refinedstorage.item.UpgradeItem;
import com.refinedmods.refinedstorage.util.StackUtils;
import com.refinedmods.refinedstorage.util.WorldUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CrafterNetworkNode extends NetworkNode implements ICraftingPatternContainer {
    private static final Logger LOGGER = LogManager.getLogger(CrafterNetworkNode.class);

    public enum CrafterMode {
        IGNORE,
        SIGNAL_UNLOCKS_AUTOCRAFTING,
        SIGNAL_LOCKS_AUTOCRAFTING,
        PULSE_INSERTS_NEXT_SET;

        public static CrafterMode getById(int id) {
            if (id >= 0 && id < values().length) {
                return values()[id];
            }

            return IGNORE;
        }
    }

    public static final ResourceLocation ID = new ResourceLocation(RS.ID, "crafter");

    private static final ITextComponent DEFAULT_NAME = new TranslationTextComponent("gui.refinedstorage.crafter");

    private static final String NBT_DISPLAY_NAME = "DisplayName";
    private static final String NBT_UUID = "CrafterUuid";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_LOCKED = "Locked";
    private static final String NBT_WAS_POWERED = "WasPowered";

    private final BaseItemHandler patternsInventory = new BaseItemHandler(9) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!stacks.get(slot).isEmpty()) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }
    }
        .addValidator(new PatternItemValidator(world))
        .addListener(new NetworkNodeInventoryListener(this))
        .addListener((handler, slot, reading) -> {
            if (!reading) {
                if (!world.isRemote) {
                    invalidate();
                }

                if (network != null) {
                    network.getCraftingManager().invalidate();
                }
            }
        });

    private final List<ICraftingPattern> patterns = new ArrayList<>();

    private final UpgradeItemHandler upgrades = (UpgradeItemHandler) new UpgradeItemHandler(4, UpgradeItem.Type.SPEED)
        .addListener(new NetworkNodeInventoryListener(this));

    // Used to prevent infinite recursion on getRootContainer() when there's e.g. two crafters facing each other.
    private boolean visited = false;

    private CrafterMode mode = CrafterMode.IGNORE;
    private boolean locked = false;
    private boolean wasPowered;

    @Nullable
    private ITextComponent displayName;

    @Nullable
    private UUID uuid = null;

    public CrafterNetworkNode(World world, BlockPos pos) {
        super(world, pos);
    }

    private void invalidate() {
        patterns.clear();

        for (int i = 0; i < patternsInventory.getSlots(); ++i) {
            ItemStack patternStack = patternsInventory.getStackInSlot(i);

            if (!patternStack.isEmpty()) {
                ICraftingPattern pattern = ((ICraftingPatternProvider) patternStack.getItem()).create(world, patternStack, this);

                if (pattern.isValid()) {
                    patterns.add(pattern);
                }
            }
        }
    }

    @Override
    public int getEnergyUsage() {
        return RS.SERVER_CONFIG.getCrafter().getUsage() + upgrades.getEnergyUsage() + (RS.SERVER_CONFIG.getCrafter().getPatternUsage() * patterns.size());
    }

    @Override
    public void update() {
        super.update();

        if (ticks == 1) {
            invalidate();
        }

        if (mode == CrafterMode.PULSE_INSERTS_NEXT_SET && world.isBlockPresent(pos)) {
            if (world.isBlockPowered(pos)) {
                this.wasPowered = true;

                markDirty();
            } else if (wasPowered) {
                this.wasPowered = false;
                this.locked = false;

                markDirty();
            }
        }
    }

    @Override
    protected void onConnectedStateChange(INetwork network, boolean state, ConnectivityStateChangeCause cause) {
        super.onConnectedStateChange(network, state, cause);

        network.getCraftingManager().invalidate();
    }

    @Override
    public void onDisconnected(INetwork network) {
        super.onDisconnected(network);

        network.getCraftingManager().getTasks().stream()
            .filter(task -> task.getPattern().getContainer().getPosition().equals(pos))
            .forEach(task -> network.getCraftingManager().cancel(task.getId()));
    }

    @Override
    public void onDirectionChanged(Direction direction) {
        super.onDirectionChanged(direction);

        if (network != null) {
            network.getCraftingManager().invalidate();
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        StackUtils.readItems(patternsInventory, 0, tag);

        invalidate();

        StackUtils.readItems(upgrades, 1, tag);

        if (tag.contains(NBT_DISPLAY_NAME)) {
            displayName = ITextComponent.Serializer.getComponentFromJson(tag.getString(NBT_DISPLAY_NAME));
        }

        if (tag.hasUniqueId(NBT_UUID)) {
            uuid = tag.getUniqueId(NBT_UUID);
        }

        if (tag.contains(NBT_MODE)) {
            mode = CrafterMode.getById(tag.getInt(NBT_MODE));
        }

        if (tag.contains(NBT_LOCKED)) {
            locked = tag.getBoolean(NBT_LOCKED);
        }

        if (tag.contains(NBT_WAS_POWERED)) {
            wasPowered = tag.getBoolean(NBT_WAS_POWERED);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        StackUtils.writeItems(patternsInventory, 0, tag);
        StackUtils.writeItems(upgrades, 1, tag);

        if (displayName != null) {
            tag.putString(NBT_DISPLAY_NAME, ITextComponent.Serializer.toJson(displayName));
        }

        if (uuid != null) {
            tag.putUniqueId(NBT_UUID, uuid);
        }

        tag.putInt(NBT_MODE, mode.ordinal());
        tag.putBoolean(NBT_LOCKED, locked);
        tag.putBoolean(NBT_WAS_POWERED, wasPowered);

        return tag;
    }

    @Override
    public int getUpdateInterval() {
        switch (upgrades.getUpgradeCount(UpgradeItem.Type.SPEED)) {
            case 0:
                return 10;
            case 1:
                return 8;
            case 2:
                return 6;
            case 3:
                return 4;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public int getMaximumSuccessfulCraftingUpdates() {
        switch (upgrades.getUpgradeCount(UpgradeItem.Type.SPEED)) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            default:
                return 1;
        }
    }

    @Override
    @Nullable
    public IItemHandler getConnectedInventory() {
        ICraftingPatternContainer proxy = getRootContainer();
        if (proxy == null) {
            return null;
        }

        return WorldUtils.getItemHandler(proxy.getFacingTile(), proxy.getDirection().getOpposite());
    }

    @Nullable
    @Override
    public IFluidHandler getConnectedFluidInventory() {
        ICraftingPatternContainer proxy = getRootContainer();
        if (proxy == null) {
            return null;
        }

        return WorldUtils.getFluidHandler(proxy.getFacingTile(), proxy.getDirection().getOpposite());
    }

    @Override
    @Nullable
    public TileEntity getConnectedTile() {
        ICraftingPatternContainer proxy = getRootContainer();
        if (proxy == null) {
            return null;
        }

        return proxy.getFacingTile();
    }

    @Nullable
    @Override
    public TileEntity getFacingTile() {
        BlockPos facingPos = pos.offset(getDirection());
        if (!world.isBlockPresent(facingPos)) {
            return null;
        }

        return world.getTileEntity(facingPos);
    }

    @Override
    public List<ICraftingPattern> getPatterns() {
        return patterns;
    }

    @Override
    @Nullable
    public IItemHandlerModifiable getPatternInventory() {
        return patternsInventory;
    }

    @Override
    public ITextComponent getName() {
        if (displayName != null) {
            return displayName;
        }

        TileEntity facing = getConnectedTile();

        if (facing instanceof INameable && ((INameable) facing).getName() != null) {
            return ((INameable) facing).getName();
        }

        if (facing != null) {
            return new TranslationTextComponent(world.getBlockState(facing.getPos()).getBlock().getTranslationKey());
        }

        return DEFAULT_NAME;
    }

    public void setDisplayName(ITextComponent displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public ITextComponent getDisplayName() {
        return displayName;
    }

    @Override
    public BlockPos getPosition() {
        return pos;
    }

    public CrafterMode getMode() {
        return mode;
    }

    public void setMode(CrafterMode mode) {
        this.mode = mode;
        this.wasPowered = false;
        this.locked = false;

        this.markDirty();
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public IItemHandler getDrops() {
        return new CombinedInvWrapper(patternsInventory, upgrades);
    }

    @Override
    @Nullable
    public ICraftingPatternContainer getRootContainer() {
        if (visited) {
            return null;
        }

        INetworkNode facing = API.instance().getNetworkNodeManager((ServerWorld) world).getNode(pos.offset(getDirection()));
        if (!(facing instanceof ICraftingPatternContainer) || facing.getNetwork() != network) {
            return this;
        }

        visited = true;
        ICraftingPatternContainer facingContainer = ((ICraftingPatternContainer) facing).getRootContainer();
        visited = false;

        return facingContainer;
    }

    public Optional<ICraftingPatternContainer> getRootContainerNotSelf() {
        ICraftingPatternContainer root = getRootContainer();

        if (root != null && root != this) {
            return Optional.of(root);
        }

        return Optional.empty();
    }

    @Override
    public UUID getUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();

            markDirty();
        }

        return uuid;
    }

    @Override
    public boolean isLocked() {
        Optional<ICraftingPatternContainer> root = getRootContainerNotSelf();
        if (root.isPresent()) {
            return root.get().isLocked();
        }

        switch (mode) {
            case IGNORE:
                return false;
            case SIGNAL_LOCKS_AUTOCRAFTING:
                return world.isBlockPowered(pos);
            case SIGNAL_UNLOCKS_AUTOCRAFTING:
                return !world.isBlockPowered(pos);
            case PULSE_INSERTS_NEXT_SET:
                return locked;
            default:
                return false;
        }
    }

    @Override
    public void unlock() {
        locked = false;
    }

    @Override
    public void onUsedForProcessing() {
        Optional<ICraftingPatternContainer> root = getRootContainerNotSelf();
        if (root.isPresent()) {
            root.get().onUsedForProcessing();

            return;
        }

        if (mode == CrafterMode.PULSE_INSERTS_NEXT_SET) {
            this.locked = true;

            markDirty();
        }
    }

    @Override
    public boolean insertItemsIntoInventory(Collection<StackListEntry<ItemStack>> toInsert, Action action) {
        IItemHandler dest = getConnectedInventory();

        if (dest == null) {
            return false;
        }

        if (toInsert.isEmpty()) {
            return true;
        }

        Deque<StackListEntry<ItemStack>> stacks = new ArrayDeque<>(toInsert);

        StackListEntry<ItemStack> currentEntry = stacks.poll();

        ItemStack current = currentEntry != null ? currentEntry.getStack() : null;

        List<Integer> availableSlots = IntStream.range(0, dest.getSlots()).boxed().collect(Collectors.toList());

        while (current != null && !availableSlots.isEmpty()) {
            ItemStack remainder = ItemStack.EMPTY;

            for (int i = 0; i < availableSlots.size(); ++i) {
                int slot = availableSlots.get(i);

                // .copy() is mandatory!
                remainder = dest.insertItem(slot, current.copy(), action == Action.SIMULATE);

                // If we inserted *something*
                if (remainder.isEmpty() || current.getCount() != remainder.getCount()) {
                    availableSlots.remove(i);
                    break;
                }
            }

            if (remainder.isEmpty()) { // If we inserted successfully, get a next stack.
                currentEntry = stacks.poll();

                current = currentEntry != null ? currentEntry.getStack() : null;
            } else if (current.getCount() == remainder.getCount()) { // If we didn't insert anything over ALL these slots, stop here.
                break;
            } else { // If we didn't insert all, continue with other slots and use our remainder.
                current = remainder;
            }
        }

        boolean success = current == null && stacks.isEmpty();

        if (!success && action == Action.PERFORM) {
            LOGGER.warn("Inventory unexpectedly didn't accept {}, the remainder has been voided!", current != null ? current.getTranslationKey() : null);
        }

        return success;
    }

    @Override
    public boolean insertFluidsIntoInventory(Collection<StackListEntry<FluidStack>> toInsert, Action action) {
        IFluidHandler dest = getConnectedFluidInventory();

        if (dest == null) {
            return false;
        }

        for (StackListEntry<FluidStack> entry : toInsert) {
            int filled = dest.fill(entry.getStack(), action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

            if (filled != entry.getStack().getAmount()) {
                if (action == Action.PERFORM) {
                    LOGGER.warn("Inventory unexpectedly didn't accept all of {}, the remainder has been voided!", entry.getStack().getTranslationKey());
                }

                return false;
            }
        }

        return true;
    }
}
