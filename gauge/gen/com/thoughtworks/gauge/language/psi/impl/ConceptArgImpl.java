// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.psi.ConceptArg;
import com.thoughtworks.gauge.language.psi.ConceptDynamicArg;
import com.thoughtworks.gauge.language.psi.ConceptStaticArg;
import com.thoughtworks.gauge.language.psi.ConceptVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConceptArgImpl extends ASTWrapperPsiElement implements ConceptArg {

    public ConceptArgImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ConceptVisitor) ((ConceptVisitor) visitor).visitArg(this);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public ConceptDynamicArg getDynamicArg() {
        return findChildByClass(ConceptDynamicArg.class);
    }

    @Override
    @Nullable
    public ConceptStaticArg getStaticArg() {
        return findChildByClass(ConceptStaticArg.class);
    }

}
