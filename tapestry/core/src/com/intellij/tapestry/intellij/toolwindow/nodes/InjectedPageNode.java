package com.intellij.tapestry.intellij.toolwindow.nodes;

import com.intellij.tapestry.core.model.presentation.InjectedElement;

import javax.swing.tree.DefaultMutableTreeNode;

public class InjectedPageNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = -937413784681186436L;

    private final transient InjectedElement _injectedPage;

    public InjectedPageNode(InjectedElement injectedPage) {
        super(injectedPage);

        _injectedPage = injectedPage;
    }

    public InjectedElement getInjectedPage() {
        return _injectedPage;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _injectedPage.getElementId();
    }
}
