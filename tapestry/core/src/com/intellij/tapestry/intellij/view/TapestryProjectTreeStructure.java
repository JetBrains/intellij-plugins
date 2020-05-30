package com.intellij.tapestry.intellij.view;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.tapestry.intellij.view.nodes.RootNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the basic tree structure.
 */
public class TapestryProjectTreeStructure extends SimpleTreeStructure {

    private static final String EMPTY_DESCRIPTOR = "EMPTY_DESCRIPTOR";
    private final RootNode _rootNode;

    protected TapestryProjectTreeStructure(final RootNode rootNode) {
        _rootNode = rootNode;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RootNode getRootElement() {
        return _rootNode;
    }

  /**
     * {@inheritDoc}
     */
    @Override
    public Object getParentElement(@NotNull final Object element) {
        try {
            return ((SimpleNode) element).getParent();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public NodeDescriptor createDescriptor(@NotNull Object element, NodeDescriptor parentDescriptor) {
        if (!(element instanceof NodeDescriptor)) {
            return new SimpleNode() {
                @Override
                public SimpleNode @NotNull [] getChildren() {
                    return new SimpleNode[0];
                }

                @Override
                public Object @NotNull [] getEqualityObjects() {
                    return new Object[]{EMPTY_DESCRIPTOR};
                }
            };
        }
        return (NodeDescriptor) element;
    }
}
