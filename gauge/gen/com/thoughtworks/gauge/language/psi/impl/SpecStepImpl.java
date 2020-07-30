package com.thoughtworks.gauge.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.helper.ModuleHelper;
import com.thoughtworks.gauge.language.psi.*;
import com.thoughtworks.gauge.reference.StepReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpecStepImpl extends SpecNamedElementImpl implements SpecStep {
    private boolean isConcept = false;
    private final ModuleHelper helper;

    public SpecStepImpl(@NotNull ASTNode node) {
        super(node);
        helper = new ModuleHelper();
    }

    public SpecStepImpl(@NotNull ASTNode node, ModuleHelper helper) {
        super(node);
        this.helper = helper;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof SpecVisitor) ((SpecVisitor) visitor).visitStep(this);
        else super.accept(visitor);
    }

    public void setConcept(boolean isConcept) {
        this.isConcept = isConcept;
    }

    @Override
    public StepValue getStepValue() {
        return SpecPsiImplUtil.getStepValue(this);
    }

    public String getName() {
        return SpecPsiImplUtil.getStepValue(this).getStepText();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String s) {
        return null;
    }

    @Override
    public ItemPresentation getPresentation() {
        return SpecPsiImplUtil.getPresentation(this);
    }

    @Override
    @Nullable
    public SpecTable getInlineTable() {
        return isConcept ? getSpecTableFromConceptStep() : findChildByClass(SpecTable.class);
    }

    private SpecTableImpl getSpecTableFromConceptStep() {
        ConceptTable conceptTable = findChildByClass(ConceptTable.class);
        return conceptTable == null ? null : new SpecTableImpl(conceptTable.getNode());
    }

    @Override
    @NotNull
    public List<SpecArg> getArgList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, SpecArg.class);
    }

    @Override
    public List<SpecStaticArg> getStaticArgList() {
        List<SpecArg> argList = getArgList();

        List<SpecStaticArg> specStaticArgs = new ArrayList<>();
        for (SpecArg arg : argList) {
            SpecStaticArg staticArg = PsiTreeUtil.getChildOfType(arg, SpecStaticArg.class);
            if (staticArg != null) {
                specStaticArgs.add(staticArg);
            }
        }
        return specStaticArgs;
    }

    @Override
    public PsiReference getReference() {
        return helper.isGaugeModule(this) ? new StepReference(this) : null;
    }
}
