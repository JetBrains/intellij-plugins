package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.util.TapestryIcons;
import com.intellij.tapestry.intellij.toolwindow.nodes.*;

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
                    setIcon(TapestryIcons.PAGE);
                    return this;
                case COMPONENT:
                    setIcon(TapestryIcons.COMPONENT);
                    return this;
                case MIXIN:
                    setIcon(TapestryIcons.MIXIN);
                    return this;
            }
        }

        if (value instanceof EmbeddedComponentsNode) {
            setIcon(TapestryIcons.COMPONENTS);
            return this;
        }

        if (value instanceof InjectedPagesNode) {
            setIcon(TapestryIcons.PAGES);
            return this;
        }

        if (value instanceof EmbeddedComponentNode) {
            setIcon(TapestryIcons.COMPONENT);
            return this;
        }

        if (value instanceof InjectedPageNode) {
            setIcon(TapestryIcons.PAGE);
            return this;
        }

        return this;
    }
}
