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
import com.thoughtworks.gauge.language.psi.ConceptConceptHeading;
import com.thoughtworks.gauge.language.psi.ConceptDynamicArg;
import com.thoughtworks.gauge.language.psi.ConceptVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConceptConceptHeadingImpl extends ASTWrapperPsiElement implements ConceptConceptHeading {

    public ConceptConceptHeadingImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ConceptVisitor) ((ConceptVisitor) visitor).visitConceptHeading(this);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<ConceptDynamicArg> getDynamicArgList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ConceptDynamicArg.class);
    }

}
