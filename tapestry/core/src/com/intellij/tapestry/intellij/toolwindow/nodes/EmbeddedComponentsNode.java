package com.intellij.tapestry.intellij.toolwindow.nodes;

import com.intellij.tapestry.core.model.presentation.*;
import com.intellij.tapestry.core.resource.IResource;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class EmbeddedComponentsNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = -3375981384098659628L;
    private final List<InjectedElement> _embeddedComponentNodes = new ArrayList<>();

    public EmbeddedComponentsNode(Object userObject) {
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
        for (TemplateElement embedded : component.getEmbeddedComponents()) {
            if (embedded.getTemplate().equals("class")) {
                add(new EmbeddedComponentNode(embedded.getElement()));
                _embeddedComponentNodes.add(embedded.getElement());
            }
        }

        for (IResource template : component.getTemplate()) {
            insert(new EmbeddedTemplateNode(template, component), 0);
            _embeddedComponentNodes.addAll(new EmbeddedTemplateNode(template, component).getEmbeddedTemplateNodes());
        }
    }

    private void addChildElements(Page page) {
        for (TemplateElement embedded : page.getEmbeddedComponents()) {
            if (embedded.getTemplate().equals("class")) {
                add(new EmbeddedComponentNode(embedded.getElement()));
                _embeddedComponentNodes.add(embedded.getElement());
            }
        }

        for (IResource template : page.getTemplate()) {
            insert(new EmbeddedTemplateNode(template, page), 0);
            _embeddedComponentNodes.addAll(new EmbeddedTemplateNode(template, page).getEmbeddedTemplateNodes());
        }

    }

    private void addChildElements(Mixin mixin) {
        for (TemplateElement embedded : mixin.getEmbeddedComponents()) {
            if (embedded.getTemplate().equals("class")) {
                add(new EmbeddedComponentNode(embedded.getElement()));
                _embeddedComponentNodes.add(embedded.getElement());
            }

        }

        for (IResource template : mixin.getTemplate()) {
            insert(new EmbeddedTemplateNode(template, mixin), 0);
            _embeddedComponentNodes.addAll(new EmbeddedTemplateNode(template, mixin).getEmbeddedTemplateNodes());
        }
    }

    public List<InjectedElement> getEmbeddedComponentNodes() {
        return _embeddedComponentNodes;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Embedded Components";
    }
}