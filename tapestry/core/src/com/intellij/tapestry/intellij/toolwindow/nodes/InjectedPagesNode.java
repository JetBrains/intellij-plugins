package com.intellij.tapestry.intellij.toolwindow.nodes;

import com.intellij.tapestry.core.model.presentation.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class InjectedPagesNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1083245064296005063L;
    private final List<InjectedElement> _injectedComponentNodes = new ArrayList<>();

    public InjectedPagesNode(Object userObject) {
        super(userObject);

        PresentationLibraryElement element = (PresentationLibraryElement) userObject;
        if (element.getElementClass().getFile() == null) {
            return;
        }

        switch (element.getElementType()) {
            case COMPONENT:
                addChildElements((TapestryComponent) userObject);
                break;
            case PAGE:
                addChildElements((Page) userObject);
                break;
            case MIXIN:
                addChildElements((Mixin) userObject);
                break;
        }
    }

    private void addChildElements(TapestryComponent component) {
        for (InjectedElement injected : component.getInjectedPages()) {
            add(new InjectedPageNode(injected));
            _injectedComponentNodes.add(injected);
        }
    }

    private void addChildElements(Page page) {
        for (InjectedElement injected : page.getInjectedPages()) {
            add(new InjectedPageNode(injected));
            _injectedComponentNodes.add(injected);
        }
    }

    private void addChildElements(Mixin mixin) {
        for (InjectedElement injected : mixin.getInjectedPages()) {
            add(new InjectedPageNode(injected));
            _injectedComponentNodes.add(injected);
        }
    }

    public List<InjectedElement> getInjectedComponentNodes() {
        return _injectedComponentNodes;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Injected Pages";
    }
}
