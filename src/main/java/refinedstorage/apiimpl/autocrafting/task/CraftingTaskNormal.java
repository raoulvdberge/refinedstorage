package refinedstorage.apiimpl.autocrafting.task;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import refinedstorage.api.autocrafting.ICraftingPattern;
import refinedstorage.api.autocrafting.task.CraftingTask;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.apiimpl.storage.fluid.FluidUtils;

import java.util.List;

public class CraftingTaskNormal extends CraftingTask {
    public CraftingTaskNormal(ICraftingPattern pattern) {
        super(pattern);
    }

    @Override
    public boolean update(World world, INetworkMaster network) {
        for (int i = 0; i < pattern.getInputs().size(); ++i) {
            checked[i] = true;

            ItemStack input = pattern.getInputs().get(i);

            if (!satisfied[i]) {
                ItemStack received = FluidUtils.extractItemOrIfBucketLookInFluids(network, input, input.stackSize);

                if (received != null) {
                    satisfied[i] = true;

                    took.add(received);

                    network.updateCraftingTasks();
                } else {
                    tryCreateChild(network, i);
                }

                break;
            }
        }

        for (boolean item : satisfied) {
            if (!item) {
                return false;
            }
        }

        for (ItemStack output : pattern.getOutputs()) {
            // @TODO: Handle remainder
            network.insertItem(output, output.stackSize, false);
        }

        return true;
    }

    @Override
    public String getStatus() {
        StringBuilder builder = new StringBuilder();

        boolean missingItems = false;

        for (int i = 0; i < pattern.getInputs().size(); ++i) {
            ItemStack input = pattern.getInputs().get(i);

            if (!satisfied[i] && !childrenCreated[i] && checked[i]) {
                if (!missingItems) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.missing_items\n");

                    missingItems = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        boolean itemsCrafting = false;

        for (int i = 0; i < pattern.getInputs().size(); ++i) {
            ItemStack input = pattern.getInputs().get(i);

            if (!satisfied[i] && childrenCreated[i] && checked[i]) {
                if (!itemsCrafting) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.items_crafting\n");

                    itemsCrafting = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        return builder.toString();
    }

    @Override
    public int getProgress() {
        int satisfiedAmount = 0;

        for (boolean item : satisfied) {
            if (item) {
                satisfiedAmount++;
            }
        }

        return (int) ((float) satisfiedAmount / (float) satisfied.length * 100F);
    }

    @Override
    public List<ItemStack> getMissingItems() {
        List<ItemStack> missingItemStacks = Lists.newArrayList();
        for (int i = 0; i < pattern.getInputs().size(); ++i) {
            ItemStack input = pattern.getInputs().get(i);
            if (!satisfied[i] && childrenCreated[i] && checked[i]) {
                missingItemStacks.add(input);
            }
        }
        return missingItemStacks;
    }
}
