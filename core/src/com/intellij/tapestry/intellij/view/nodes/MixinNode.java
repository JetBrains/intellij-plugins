package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.tapestry.core.model.presentation.Mixin;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.Icons;

/**
 * A Mixin node.
 */
public class MixinNode extends TapestryNode {

    public MixinNode(PresentationLibraryElement mixin, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(mixin, new PresentationData(mixin.getName(), mixin.getName(), Icons.MIXIN, Icons.MIXIN, null));
    }

    /**
     * {@inheritDoc}
     */
    public SimpleNode[] getChildren() {
        Mixin mixin = (Mixin) getElement();

        return new SimpleNode[]{new ClassNode((PsiJavaFile) ((IntellijJavaClassType) mixin.getElementClass()).getPsiClass().getContainingFile(), getModule(), _treeBuilder)};
    }
}
