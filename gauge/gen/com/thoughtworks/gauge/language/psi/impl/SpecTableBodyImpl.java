// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.language.psi.SpecTableBody;
import com.thoughtworks.gauge.language.psi.SpecTableRowValue;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpecTableBodyImpl extends ASTWrapperPsiElement implements SpecTableBody {

    public SpecTableBodyImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) ((SpecVisitor) visitor).visitTableBody(this);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public List<SpecTableRowValue> getTableRowValueList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, SpecTableRowValue.class);
    }

}
