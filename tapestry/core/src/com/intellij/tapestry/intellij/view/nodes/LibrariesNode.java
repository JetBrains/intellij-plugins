package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.ui.treeStructure.SimpleNode;

import java.util.ArrayList;
import java.util.List;

public class LibrariesNode extends TapestryNode {

    public LibrariesNode(Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init("Libraries", new PresentationData("Libraries", "Libraries", AllIcons.Modules.Library, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode[] getChildren() {
        List<TapestryNode> children = new ArrayList<>();

        for (Library library : TapestryModuleSupportLoader.getTapestryProject(_module).getLibraries()) {
            if (!library.getId().equals(TapestryProject.APPLICATION_LIBRARY_ID)) {
                children.add(new ExternalLibraryNode(library, _module, _treeBuilder));
            }
        }

        return children.toArray(new TapestryNode[0]);
    }
}
