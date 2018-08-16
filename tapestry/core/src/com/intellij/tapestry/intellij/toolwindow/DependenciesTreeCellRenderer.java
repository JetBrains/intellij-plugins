package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.toolwindow.nodes.*;
import icons.TapestryIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class DependenciesTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DependenciesRootNode) {
            switch (((PresentationLibraryElement) ((DefaultMutableTreeNode) value).getUserObject()).getElementType()) {
                case PAGE:
                    setIcon(TapestryIcons.Page);
                    return this;
                case COMPONENT:
                    setIcon(TapestryIcons.Component);
                    return this;
                case MIXIN:
                    setIcon(TapestryIcons.Mixin);
                    return this;
            }
        }

        if (value instanceof EmbeddedComponentsNode) {
            setIcon(TapestryIcons.Components);
            return this;
        }

        if (value instanceof InjectedPagesNode) {
            setIcon(TapestryIcons.Pages);
            return this;
        }

        if (value instanceof EmbeddedComponentNode) {
            setIcon(TapestryIcons.Component);
            return this;
        }

        if (value instanceof InjectedPageNode) {
            setIcon(TapestryIcons.Page);
            return this;
        }

        return this;
    }
}
