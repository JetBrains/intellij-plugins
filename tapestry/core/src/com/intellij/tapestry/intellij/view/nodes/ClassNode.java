package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClassOwner;
import com.intellij.util.PlatformIcons;

public class ClassNode extends TapestryNode {

    public ClassNode(PsiClassOwner psiClassOwner, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(psiClassOwner, new PresentationData(psiClassOwner.getName(), psiClassOwner.getName(), PlatformIcons.CLASS_ICON, null));
    }
}
