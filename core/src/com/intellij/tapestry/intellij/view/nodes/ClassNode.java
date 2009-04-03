package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiJavaFile;
import com.intellij.util.Icons;

public class ClassNode extends TapestryNode {

    public ClassNode(PsiJavaFile psiJavaFile, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(psiJavaFile, new PresentationData(psiJavaFile.getName(), psiJavaFile.getName(), Icons.CLASS_ICON, Icons.CLASS_ICON, null));
    }
}
