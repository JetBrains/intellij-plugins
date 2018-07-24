package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;

public class FileNode extends TapestryNode {

    public FileNode(PsiFile file, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(file, new PresentationData(file.getName(), file.getName(), file.getFileType().getIcon(), null));
    }
}
