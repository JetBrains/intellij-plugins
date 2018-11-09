package com.intellij.tapestry.intellij.view;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.StatusBarProgress;
import com.intellij.openapi.project.Project;
import com.intellij.tapestry.intellij.view.nodes.RootNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
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

    @Override
    protected boolean isDisposeOnCollapsing(NodeDescriptor nodeDescriptor) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getTreeStructureElement(NodeDescriptor nodeDescriptor) {
        return nodeDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProgressIndicator createProgressIndicator() {
        return new StatusBarProgress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlwaysShowPlus(@Nullable NodeDescriptor nodeDescriptor) {
        return nodeDescriptor == null || ((SimpleNode) nodeDescriptor).isAlwaysShowPlus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoExpandNode(@Nullable NodeDescriptor nodeDescriptor) {
        return nodeDescriptor != null && ((SimpleNode) nodeDescriptor).isAutoExpandNode();
    }
}
