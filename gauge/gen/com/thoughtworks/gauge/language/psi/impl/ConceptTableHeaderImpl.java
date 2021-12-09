// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.psi.ConceptTableHeader;
import com.thoughtworks.gauge.language.psi.ConceptVisitor;
import org.jetbrains.annotations.NotNull;

public class ConceptTableHeaderImpl extends ASTWrapperPsiElement implements ConceptTableHeader {

    public ConceptTableHeaderImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ConceptVisitor) ((ConceptVisitor) visitor).visitTableHeader(this);
        else super.accept(visitor);
    }

}
