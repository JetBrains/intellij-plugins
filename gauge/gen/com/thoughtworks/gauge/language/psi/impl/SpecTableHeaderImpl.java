// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.psi.SpecTableHeader;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpecTableHeaderImpl extends ASTWrapperPsiElement implements SpecTableHeader {

    public SpecTableHeaderImpl(ASTNode node) {
        super(node);
    }

    @Override
    public List<String> getHeaders() {
        List<PsiElement> headers = findChildrenByType(SpecTokenTypes.TABLE_HEADER);
        List<String> headerValue = new ArrayList<>();
        for (PsiElement header : headers) {
            headerValue.add(header.getText().trim());
        }
        return headerValue;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) ((SpecVisitor) visitor).visitTableHeader(this);
        else super.accept(visitor);
    }

}
