package com.refinedmods.refinedstorage.screen.grid.view;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.grid.filtering.GridFilterParser;
import com.refinedmods.refinedstorage.screen.grid.sorting.IGridSorter;
import com.refinedmods.refinedstorage.screen.grid.sorting.SortingDirection;
import com.refinedmods.refinedstorage.screen.grid.stack.IGridStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GridViewImpl implements IGridView {
    private final GridScreen screen;
    private boolean canCraft;
    private boolean active = false;

    private final IGridSorter defaultSorter;
    private final List<IGridSorter> sorters;

    private List<IGridStack> stacks = new ArrayList<>();
    protected final Map<UUID, IGridStack> map = new HashMap<>();

    public GridViewImpl(GridScreen screen, IGridSorter defaultSorter, List<IGridSorter> sorters) {
        this.screen = screen;
        this.defaultSorter = defaultSorter;
        this.sorters = sorters;
    }

    @Override
    public List<IGridStack> getStacks() {
        return stacks;
    }

    @Override
    public Collection<IGridStack> getAllStacks() {
        return map.values();
    }

    @Nullable
    @Override
    public IGridStack get(UUID id) {
        return map.get(id);
    }

    @Override
    public void sort() {
        if (!screen.canSort()) {
            return;
        }

        if (screen.getGrid().isGridActive()) {
            this.stacks = map.values().stream()
                    .filter(getActiveFilters())
                    .sorted(getActiveSort())
                    .collect(Collectors.toCollection(ArrayList::new));
            this.active = true;
        } else {
            this.stacks = new ArrayList<>();
            this.active = false;
        }

        this.screen.updateScrollbar();
    }

    private Comparator<IGridStack> getActiveSort() {
        IGrid grid = screen.getGrid();
        SortingDirection sortingDirection = grid.getSortingDirection() == IGrid.SORTING_DIRECTION_DESCENDING ? SortingDirection.DESCENDING : SortingDirection.ASCENDING;
        return Stream.concat(Stream.of(defaultSorter), sorters.stream().filter(s -> s.isApplicable(grid)))
                .map(sorter -> (Comparator<IGridStack>) (o1, o2) -> sorter.compare(o1, o2, sortingDirection))
                .reduce((l, r) -> r.thenComparing(l))
                .orElseThrow(IllegalStateException::new);  // There is at least 1 value in the stream (i.e. defaultSorter)
    }

    private Predicate<IGridStack> getActiveFilters() {
        IGrid grid = screen.getGrid();

        Predicate<IGridStack> filters = GridFilterParser.getFilters(
                grid,
                screen.getSearchFieldText(),
                (grid.getTabSelected() >= 0 && grid.getTabSelected() < grid.getTabs().size()) ? grid.getTabs().get(grid.getTabSelected()).getFilters() : grid.getFilters()
        );

        if (screen.getGrid().getViewType() != IGrid.VIEW_TYPE_CRAFTABLES) {
            return stack -> {
                // If this is a crafting stack,
                // and there is a regular matching stack in the view too,
                // and we aren't in "view only craftables" mode,
                // we don't want the duplicate stacks and we will remove this stack.
                if (stack.isCraftable() &&
                        stack.getOtherId() != null &&
                        map.containsKey(stack.getOtherId())) {
                    return false;
                }

                return filters.test(stack);
            };
        } else {
            return filters;
        }
    }

    @Override
    public void setStacks(List<IGridStack> stacks) {
        map.clear();

        for (IGridStack stack : stacks) {
            map.put(stack.getId(), stack);
        }
    }

    @Override
    public void postChange(IGridStack stack, int delta) {
        if (!this.active) {
            return;
        }
        // COMMENT 1 (about this if check in general)
        // Update the other id reference if needed.
        // Taking a stack out - and then re-inserting it - gives the new stack a new ID
        // With that new id, the reference for the crafting stack would be outdated.

        // COMMENT 2 (about map.containsKey(stack.getOtherId()))
        // This check is needed or the .updateOtherId() call will crash with a NPE in high-update environments.
        // This is because we might have scenarios where we process "old" delta packets from another session when we haven't received any initial update packet from the new session.
        // (This is because of the executeLater system)
        // This causes the .updateOtherId() to fail with a NPE because the map is still empty or the IDs mismatch.
        // We could use !map.isEmpty() here too. But if we have 2 "old" delta packets, it would rightfully ignore the first one. But this method mutates the map and would put an entry.
        // This means that on the second delta packet it would still crash because the map wouldn't be empty anymore.
        IGridStack craftingStack;
        if (!stack.isCraftable() &&
                stack.getOtherId() != null &&
                map.containsKey(stack.getOtherId())) {
            craftingStack = map.get(stack.getOtherId());

            craftingStack.updateOtherId(stack.getId());
            craftingStack.setTrackerEntry(stack.getTrackerEntry());
        } else {
            craftingStack = null;
        }

        Predicate<IGridStack> activeFilters = getActiveFilters();
        IGridStack existing = map.get(stack.getId());
        boolean stillExists = true;
        boolean shouldSort = screen.canSort();

        if (existing == null) {
            stack.setQuantity(delta);

            map.put(stack.getId(), stack);
            existing = stack;

            if (craftingStack != null && shouldSort && activeFilters.test(existing)) {
                stacks.remove(craftingStack);
            }
        } else {
            if (shouldSort) {
                stacks.remove(existing);
            }
            existing.setQuantity(existing.getQuantity() + delta);
            if (existing.getQuantity() <= 0) {
                map.remove(existing.getId());
                stillExists = false;

                if (craftingStack != null && shouldSort && activeFilters.test(existing) && activeFilters.test(craftingStack)) {
                    addStack(craftingStack);
                }
            }

            existing.setTrackerEntry(stack.getTrackerEntry());
        }

        if (shouldSort) {
            if (stillExists && activeFilters.test(existing)) {
                addStack(existing);
            }
            this.screen.updateScrollbar();
        }
    }

    private void addStack(IGridStack stack) {
        int insertionPos = Collections.binarySearch(stacks, stack, getActiveSort());
        if (insertionPos < 0) {
            insertionPos = -insertionPos - 1;
        }
        stacks.add(insertionPos, stack);
    }

    @Override
    public void setCanCraft(boolean canCraft) {
        this.canCraft = canCraft;
    }

    @Override
    public boolean canCraft() {
        return canCraft;
    }
}
