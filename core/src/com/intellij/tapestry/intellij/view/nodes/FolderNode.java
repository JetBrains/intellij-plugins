package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.core.util.TapestryIcons;

public class FolderNode extends TapestryNode {

    private Class _classToCreate;
    private Library _library;

    public FolderNode(String folder, Library library, Class classToCreate, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(folder, new PresentationData(PathUtils.getLastPathElement(folder), folder, TapestryIcons.FOLDER, TapestryIcons.FOLDER, null));

        _classToCreate = classToCreate;
        _library = library;
    }

    public Library getLibrary() {
        return _library;
    }

    public Class getClassToCreate() {
        return _classToCreate;
    }
}
