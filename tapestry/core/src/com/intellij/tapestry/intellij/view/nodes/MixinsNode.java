package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;
import com.intellij.tapestry.core.model.TapestryLibrary;
import icons.TapestryIcons;

public class MixinsNode extends PackageNode {

    public MixinsNode(TapestryLibrary library, PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        super(library, psiDirectory, module, treeBuilder);

        init(psiDirectory, new PresentationData(psiDirectory.getName(), psiDirectory.getName(), TapestryIcons.Mixins, null));
    }

    public MixinsNode(PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        super(psiDirectory, module, treeBuilder);

        init(psiDirectory, new PresentationData(psiDirectory.getName(), psiDirectory.getName(), TapestryIcons.Mixins, null));
    }
}
