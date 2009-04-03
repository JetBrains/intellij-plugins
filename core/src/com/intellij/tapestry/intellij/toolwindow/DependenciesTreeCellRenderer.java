package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.toolwindow.nodes.*;
import com.intellij.tapestry.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class DependenciesTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * {@inheritDoc}
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DependenciesRootNode) {
            switch (((PresentationLibraryElement) ((DefaultMutableTreeNode) value).getUserObject()).getElementType()) {
                case PAGE:
                    setIcon(Icons.PAGE);
                    return this;
                case COMPONENT:
                    setIcon(Icons.COMPONENT);
                    return this;
                case MIXIN:
                    setIcon(Icons.MIXIN);
                    return this;
            }
        }

        if (value instanceof EmbeddedComponentsNode) {
            setIcon(Icons.COMPONENTS);
            return this;
        }

        if (value instanceof InjectedPagesNode) {
            setIcon(Icons.PAGES);
            return this;
        }

        if (value instanceof EmbeddedComponentNode) {
            setIcon(Icons.COMPONENT);
            return this;
        }

        if (value instanceof InjectedPageNode) {
            setIcon(Icons.PAGE);
            return this;
        }

        return this;
    }
}
