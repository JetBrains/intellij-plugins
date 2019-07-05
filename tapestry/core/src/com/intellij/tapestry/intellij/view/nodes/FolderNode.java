package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.util.PathUtils;
import icons.TapestryIcons;

public class FolderNode extends TapestryNode {

    private final Class _classToCreate;
    private final TapestryLibrary _library;

    public FolderNode(String folder, TapestryLibrary library, Class classToCreate, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(folder, new PresentationData(PathUtils.getLastPathElement(folder), folder, TapestryIcons.Folder, null));

        _classToCreate = classToCreate;
        _library = library;
    }

    public TapestryLibrary getLibrary() {
        return _library;
    }

    public Class getClassToCreate() {
        return _classToCreate;
    }
}
