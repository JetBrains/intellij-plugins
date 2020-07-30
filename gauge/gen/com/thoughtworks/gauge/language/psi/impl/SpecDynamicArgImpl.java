// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.language.psi.SpecDynamicArg;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;

public class SpecDynamicArgImpl extends ASTWrapperPsiElement implements SpecDynamicArg {

    public SpecDynamicArgImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getText() {
        PsiElement arg = findChildByType(SpecTokenTypes.DYNAMIC_ARG);
        return arg != null ? arg.getText() : null;
    }
}
