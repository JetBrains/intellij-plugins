package com.intellij.tapestry.intellij.toolwindow.nodes;

import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;

import javax.swing.tree.DefaultMutableTreeNode;

public class DependenciesRootNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 7908744831198159929L;

    public DependenciesRootNode(Object userObject) {
        super(userObject);

        insert(new EmbeddedComponentsNode(userObject), 0);
        insert(new InjectedPagesNode(userObject), 0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ((PresentationLibraryElement) getUserObject()).getName();
    }
}
