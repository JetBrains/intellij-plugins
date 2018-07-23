package com.intellij.tapestry.intellij.toolwindow.nodes;

import com.intellij.tapestry.core.model.presentation.InjectedElement;

import javax.swing.tree.DefaultMutableTreeNode;

public class EmbeddedComponentNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 8480011580669274491L;

    private final transient InjectedElement _injectedComponent;

    public EmbeddedComponentNode(InjectedElement injectedComponent) {
        super(injectedComponent);

        _injectedComponent = injectedComponent;
    }

    public InjectedElement getInjectedComponent() {
        return _injectedComponent;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _injectedComponent.getElementId();
    }
}
