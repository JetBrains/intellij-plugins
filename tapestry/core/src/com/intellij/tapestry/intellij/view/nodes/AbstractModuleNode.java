package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractModuleNode extends TapestryNode {

    public AbstractModuleNode(@NotNull final Module module, final AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(module, new PresentationData(module.getName(), module.getName(), PlatformIcons.WEB_ICON, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract SimpleNode @NotNull [] getChildren();
}
