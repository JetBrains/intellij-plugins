// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.psi.SpecArg;
import com.thoughtworks.gauge.language.psi.SpecDynamicArg;
import com.thoughtworks.gauge.language.psi.SpecStaticArg;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecArgImpl extends ASTWrapperPsiElement implements SpecArg {

    public SpecArgImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) ((SpecVisitor) visitor).visitArg(this);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public SpecDynamicArg getDynamicArg() {
        return findChildByClass(SpecDynamicArg.class);
    }

    @Override
    @Nullable
    public SpecStaticArg getStaticArg() {
        return findChildByClass(SpecStaticArg.class);
    }

}
