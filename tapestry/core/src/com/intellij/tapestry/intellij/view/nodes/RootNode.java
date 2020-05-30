package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tapestry view root node.
 */
public class RootNode extends SimpleNode {

    private static final String ID = "ROOT";
    private static final SimpleNode[] EMPTY_LIST = new SimpleNode[]{};
    protected AbstractTreeBuilder _treeBuilder;
    protected Object _myElement;

    public RootNode(@NotNull final Project project) {
        super(project);

        _myElement = ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getElement() {
        return _myElement;
    }

    public void setTreeBuilder(AbstractTreeBuilder treeBuilder) {
        _treeBuilder = treeBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode @NotNull [] getChildren() {
        if (_treeBuilder == null) {
            return EMPTY_LIST;
        }

        final List<AbstractModuleNode> newNodes = new ArrayList<>();
        final Module[] allTapestryModules = TapestryUtils.getAllTapestryModules(myProject);

        for (final Module module : allTapestryModules)
            newNodes.add(new ModuleNode(module, _treeBuilder));


        return newNodes.toArray(new AbstractModuleNode[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object @NotNull [] getEqualityObjects() {
        return new Object[]{_myElement};
    }
}
