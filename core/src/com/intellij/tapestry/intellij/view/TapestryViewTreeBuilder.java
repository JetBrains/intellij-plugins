package com.intellij.tapestry.intellij.view;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.StatusBarProgress;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.tapestry.intellij.view.nodes.RootNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

/**
 * The Tapestry view tree builder.
 */
public class TapestryViewTreeBuilder extends SimpleTreeBuilder {

    public TapestryViewTreeBuilder(JTree tree, Project project) {
        super(tree, (DefaultTreeModel) tree.getModel(), new TapestryProjectTreeStructure(new RootNode(project)), null);

        RootNode rootNode = ((TapestryProjectTreeStructure) getTreeStructure()).getRootElement();
        rootNode.setTreeBuilder(this);
    }

    protected boolean isDisposeOnCollapsing(NodeDescriptor nodeDescriptor) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected Object getTreeStructureElement(NodeDescriptor nodeDescriptor) {
        return nodeDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    protected ProgressIndicator createProgressIndicator() {
        return new StatusBarProgress();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAlwaysShowPlus(@Nullable NodeDescriptor nodeDescriptor) {
        return nodeDescriptor == null || ((SimpleNode) nodeDescriptor).isAlwaysShowPlus();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAutoExpandNode(@Nullable NodeDescriptor nodeDescriptor) {
        return nodeDescriptor != null && ((SimpleNode) nodeDescriptor).isAutoExpandNode();
    }
}
