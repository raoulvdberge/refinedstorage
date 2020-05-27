package com.refinedmods.refinedstorage.tile.craftingmonitor;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingManager;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.refinedmods.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ICraftingMonitor {
    int TABS_PER_PAGE = 7;

    ITextComponent getTitle();

    void onCancelled(ServerPlayerEntity player, @Nullable UUID id);

    TileDataParameter<Integer, ?> getRedstoneModeParameter();

    Collection<ICraftingTask> getTasks();

    @Nullable
    ICraftingManager getCraftingManager();

    boolean isActiveOnClient();

    void onClosed(PlayerEntity player);

    Optional<UUID> getTabSelected();

    int getTabPage();

    void onTabSelectionChanged(Optional<UUID> taskId);

    void onTabPageChanged(int page);

    int getSlotId();
}
