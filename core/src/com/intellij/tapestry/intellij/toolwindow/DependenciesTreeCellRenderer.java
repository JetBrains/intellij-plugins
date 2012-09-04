package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.toolwindow.nodes.*;
import icons.TapestryCoreIcons;

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
                    setIcon(TapestryCoreIcons.Page);
                    return this;
                case COMPONENT:
                    setIcon(TapestryCoreIcons.Component);
                    return this;
                case MIXIN:
                    setIcon(TapestryCoreIcons.Mixin);
                    return this;
            }
        }

        if (value instanceof EmbeddedComponentsNode) {
            setIcon(TapestryCoreIcons.Components);
            return this;
        }

        if (value instanceof InjectedPagesNode) {
            setIcon(TapestryCoreIcons.Pages);
            return this;
        }

        if (value instanceof EmbeddedComponentNode) {
            setIcon(TapestryCoreIcons.Component);
            return this;
        }

        if (value instanceof InjectedPageNode) {
            setIcon(TapestryCoreIcons.Page);
            return this;
        }

        return this;
    }
}
