/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/

package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.language.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConceptConceptImpl extends ASTWrapperPsiElement implements ConceptConcept {

    public ConceptConceptImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ConceptVisitor) ((ConceptVisitor) visitor).visitConcept(this);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public ConceptConceptHeading getConceptHeading() {
        return findNotNullChildByClass(ConceptConceptHeading.class);
    }

    @Override
    @NotNull
    public List<ConceptStep> getStepList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ConceptStep.class);
    }

    public StepValue getStepValue() {
        return ConceptPsiImplUtil.getStepValue(this);
    }

}
