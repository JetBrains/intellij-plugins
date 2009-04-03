package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.util.PathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Base class for the pages, components and mixins nodes.
 */
public abstract class AbstractPresentationNode extends TapestryNode {

    public AbstractPresentationNode(Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);
    }

    /**
     * {@inheritDoc}
     */
    public SimpleNode[] getChildren() {
        TreeSet<TapestryNode> children = new TreeSet<TapestryNode>(PackageNodesComparator.getInstance());
        List<String> addedFolders = new ArrayList<String>();

        for (PresentationLibraryElement element : getChildElements())
            if (element.getName().contains(PathUtils.TAPESTRY_PATH_SEPARATOR)) {
                String folderName = PathUtils.getFirstPathElement(element.getName());
                if (!addedFolders.contains(folderName)) {
                    children.add(new FolderNode(folderName, (Library) getElement(), getChildNodeClass(), _module, _treeBuilder));
                    addedFolders.add(folderName);
                }
            } else {
                children.add(newChildNode(element, _module, _treeBuilder));
            }

        return children.toArray(new SimpleNode[children.size()]);
    }

    /**
     * Creates a new child node.
     *
     * @param element     the Tapestry element of the child node.
     * @param module      the module of the child node.
     * @param treeBuilder the tree builder.
     * @return a new child node.
     */
    protected abstract TapestryNode newChildNode(PresentationLibraryElement element, Module module, AbstractTreeBuilder treeBuilder);

    /**
     * Finds every child component.
     *
     * @return list of the child components.
     */
    protected abstract Collection<PresentationLibraryElement> getChildElements();

    /**
     * Returns the child node class.
     *
     * @return the child node class.
     */
    protected abstract Class getChildNodeClass();
}
