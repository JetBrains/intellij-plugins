// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.language.psi.SpecTeardown;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpecTeardownImpl extends ASTWrapperPsiElement implements SpecTeardown {

    public SpecTeardownImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull SpecVisitor visitor) {
        visitor.visitTeardown(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) accept((SpecVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<SpecStep> getStepList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, SpecStep.class);
    }

}
