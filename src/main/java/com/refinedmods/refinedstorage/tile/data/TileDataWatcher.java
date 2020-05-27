package com.refinedmods.refinedstorage.tile.data;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.network.tiledata.TileDataParameterMessage;
import net.minecraft.entity.player.ServerPlayerEntity;

public class TileDataWatcher {
    private boolean sentInitial;
    private ServerPlayerEntity player;
    private TileDataManager manager;

    private Object[] cache;

    public TileDataWatcher(ServerPlayerEntity player, TileDataManager manager) {
        this.player = player;
        this.manager = manager;

        if (manager != null) {
            this.manager.addWatcher(this);
            this.cache = new Object[manager.getWatchedParameters().size()];
        }
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void onClosed() {
        this.manager.removeWatcher(this);
    }

    public void detectAndSendChanges() {
        if (!sentInitial) {
            manager.getParameters().forEach(p -> sendParameter(true, p));

            sentInitial = true;
        } else {
            for (int i = 0; i < manager.getWatchedParameters().size(); ++i) {
                TileDataParameter parameter = manager.getWatchedParameters().get(i);

                Object real = parameter.getValueProducer().apply(manager.getTile());
                Object cached = cache[i];

                if (!real.equals(cached)) {
                    cache[i] = real;

                    // Avoid sending watched parameter twice (after initial packet)
                    if (cached != null) {
                        sendParameter(false, parameter);
                    }
                }
            }
        }
    }

    public void sendParameter(boolean initial, TileDataParameter parameter) {
        RS.NETWORK_HANDLER.sendTo(player, new TileDataParameterMessage(manager.getTile(), parameter, initial));
    }
}
