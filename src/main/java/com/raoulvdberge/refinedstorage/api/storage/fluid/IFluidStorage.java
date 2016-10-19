package com.raoulvdberge.refinedstorage.api.storage.fluid;

import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a fluid storage sink for the storage network.
 * Provide this through an {@link IFluidStorageProvider}.
 */
public interface IFluidStorage {
    /**
     * @return fluids stored in this storage
     */
    List<FluidStack> getStacks();

    /**
     * Inserts a fluid in this storage.
     *
     * @param stack    the fluid prototype to insert, do NOT modify
     * @param size     the amount of that prototype that has to be inserted
     * @param simulate true if we are simulating, false otherwise
     * @return null if the insert was successful, or a stack with the remainder
     */
    @Nullable
    FluidStack insertFluid(@Nonnull FluidStack stack, int size, boolean simulate);

    /**
     * Extracts a fluid from this storage.
     * <p>
     * If the fluid we found in the system is smaller than the requested size, return that fluid anyway.
     *
     * @param stack a prototype of the fluid to extract, do NOT modify
     * @param size  the amount of that fluid that has to be extracted
     * @param flags the flags to compare on, see {@link IComparer}
     * @return null if we didn't extract anything, or a stack with the result
     */
    @Nullable
    FluidStack extractFluid(@Nonnull FluidStack stack, int size, int flags);

    /**
     * @return the amount of fluids stored in this storage
     */
    int getStored();

    /**
     * @return the priority of this storage
     */
    int getPriority();

    /**
     * @return the access type of this storage
     */
    default AccessType getAccessType() {
        return AccessType.READ_WRITE;
    }
}
