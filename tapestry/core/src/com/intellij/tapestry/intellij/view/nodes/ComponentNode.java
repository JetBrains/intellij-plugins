package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClassOwner;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.ui.treeStructure.SimpleNode;
import icons.TapestryIcons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Component node.
 */
public class ComponentNode extends TapestryNode {

    public ComponentNode(PresentationLibraryElement component, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(component, new PresentationData(component.getElementClass().getName(), component.getElementClass().getName(),
                                             TapestryIcons.Component, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode @NotNull [] getChildren() {
        TapestryComponent component = (TapestryComponent) getElement();
        List<SimpleNode> children = new ArrayList<>();

        ClassNode classNode = new ClassNode((PsiClassOwner) ((IntellijJavaClassType) component.getElementClass()).getPsiClass().getContainingFile(), getModule(), _treeBuilder);
        children.add(classNode);

        for (IResource template : component.getTemplate())
            children.add(new FileNode(((IntellijResource) template).getPsiFile(), getModule(), _treeBuilder));

        for (IResource catalog : component.getMessageCatalog())
            children.add(new FileNode(((IntellijResource) catalog).getPsiFile(), getModule(), _treeBuilder));

        return children.toArray(new SimpleNode[0]);
    }
}
