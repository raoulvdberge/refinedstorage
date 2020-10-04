package com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.node;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.apiimpl.autocrafting.task.v6.CraftingPatternInputs;

import java.util.*;

public class NodeList {
    private final Map<ICraftingPattern, Node> nodes = new LinkedHashMap<>();
    private final List<Node> nodesToRemove = new ArrayList<>();

    public void removeMarkedForRemoval() {
        for (Node node : nodesToRemove) {
            nodes.remove(node.getPattern());
        }
        nodesToRemove.clear();
    }

    public Collection<Node> all() {
        return nodes.values();
    }

    public void unlockAll(INetwork network) {
        for (Node node : nodes.values()) {
            if (node instanceof ProcessingNode) {
                network.getCraftingManager().getAllContainers(node.getPattern()).forEach(ICraftingPatternContainer::unlock);
            }
        }
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void remove(Node node) {
        nodesToRemove.add(node);
    }

    public Node createOrAddToExistingNode(ICraftingPattern pattern, boolean root, CraftingPatternInputs inputs, int qty) {
        Node node = nodes.get(pattern);
        if (node == null) {
            nodes.put(pattern, node = createNode(pattern, root, inputs));
        }

        node.addQuantity(qty);

        return node;
    }

    private Node createNode(ICraftingPattern pattern, boolean root, CraftingPatternInputs inputs) {
        return pattern.isProcessing() ? new ProcessingNode(pattern, root, inputs) : new CraftingNode(pattern, root, inputs);
    }

    public void put(ICraftingPattern pattern, Node node) {
        nodes.put(pattern, node);
    }
}
