package com.refinedmods.refinedstorage.render.collision;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CollisionGroup {

    private List<AxisAlignedBB> items = new ArrayList<>();
    private boolean canAccessGui;
    @Nullable
    private Direction direction;

    public CollisionGroup addItem(AxisAlignedBB item) {
        items.add(item);

        return this;
    }

    public List<AxisAlignedBB> getItems() {
        return items;
    }

    public boolean canAccessGui() {
        return canAccessGui;
    }

    public CollisionGroup setCanAccessGui(boolean canAccessGui) {
        this.canAccessGui = canAccessGui;

        return this;
    }

    public CollisionGroup setDirection(Direction direction) {
        this.direction = direction;

        return this;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }
    
}
