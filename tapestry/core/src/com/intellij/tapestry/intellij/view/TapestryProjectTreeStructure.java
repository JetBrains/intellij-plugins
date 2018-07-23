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
    public RootNode getRootElement() {
        return _rootNode;
    }

  /**
     * {@inheritDoc}
     */
    public Object getParentElement(final Object element) {
        try {
            return ((SimpleNode) element).getParent();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public NodeDescriptor createDescriptor(Object element, NodeDescriptor parentDescriptor) {
        if (!(element instanceof NodeDescriptor)) {
            return new SimpleNode() {
                public SimpleNode[] getChildren() {
                    return new SimpleNode[0];
                }

                @NotNull
                public Object[] getEqualityObjects() {
                    return new Object[]{EMPTY_DESCRIPTOR};
                }
            };
        }
        return (NodeDescriptor) element;
    }
}
