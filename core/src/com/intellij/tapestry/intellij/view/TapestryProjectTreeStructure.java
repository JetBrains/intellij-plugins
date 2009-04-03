package com.intellij.tapestry.intellij.view;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.tapestry.intellij.view.nodes.RootNode;

/**
 * Defines the basic tree structure.
 */
public class TapestryProjectTreeStructure extends SimpleTreeStructure {

    private final static String EMPTY_DESCRIPTOR = "EMPTY_DESCRIPTOR";
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
    public boolean hasSomethingToCommit() {
        return false;
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
    public NodeDescriptor createDescriptor(Object element, NodeDescriptor parentDescriptor) {
        if (element == null || !(element instanceof NodeDescriptor)) {
            return new SimpleNode() {
                public SimpleNode[] getChildren() {
                    return new SimpleNode[0];
                }

                public Object[] getEqualityObjects() {
                    return new Object[]{EMPTY_DESCRIPTOR};
                }
            };
        }
        return (NodeDescriptor) element;
    }
}
