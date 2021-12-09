// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.language.psi.SpecDetail;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.language.psi.SpecTable;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpecDetailImpl extends ASTWrapperPsiElement implements SpecDetail {

    public SpecDetailImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) ((SpecVisitor) visitor).visitSpecDetail(this);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<SpecStep> getContextSteps() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, SpecStep.class);
    }

    @Override
    @Nullable
    public SpecTable getDataTable() {
        return findChildByClass(SpecTable.class);
    }

}
