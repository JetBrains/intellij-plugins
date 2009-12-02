package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.Icons;

import java.util.ArrayList;
import java.util.List;

/**
 * Component node.
 */
public class ComponentNode extends TapestryNode {

    public ComponentNode(PresentationLibraryElement component, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(component, new PresentationData(component.getElementClass().getName(), component.getElementClass().getName(), Icons.COMPONENT, Icons.COMPONENT, null));
    }

    /**
     * {@inheritDoc}
     */
    public SimpleNode[] getChildren() {
        Component component = (Component) getElement();
        List<SimpleNode> children = new ArrayList<SimpleNode>();

        ClassNode classNode = new ClassNode((PsiClassOwner) ((IntellijJavaClassType) component.getElementClass()).getPsiClass().getContainingFile(), getModule(), _treeBuilder);
        children.add(classNode);

        for (IResource template : component.getTemplate())
            children.add(new FileNode(((IntellijResource) template).getPsiFile(), getModule(), _treeBuilder));

        for (IResource catalog : component.getMessageCatalog())
            children.add(new FileNode(((IntellijResource) catalog).getPsiFile(), getModule(), _treeBuilder));

        return children.toArray(new SimpleNode[0]);
    }
}
