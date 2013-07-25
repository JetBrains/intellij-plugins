package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 23.04.2009
 * Time: 19:52:20
 * To change this template use File | Settings | File Templates.
 */
public class CfmlCompositeElementType extends IElementType implements ICompositeElementType {
    public CfmlCompositeElementType(@NotNull @NonNls final String debugName) {
        super(debugName, CfmlLanguage.INSTANCE);
    }
    
    public PsiElement createPsiElement(ASTNode node) {
        return new CfmlCompositeElement(node);
    }

    @NotNull
    public ASTNode createCompositeNode() {
        return new CompositeElement(this);
    }
}
