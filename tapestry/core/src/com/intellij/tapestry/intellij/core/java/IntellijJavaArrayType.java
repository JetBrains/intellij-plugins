package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiArrayType;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;

public class IntellijJavaArrayType extends IntellijJavaType implements IJavaArrayType {

    private final Module _module;
    private final PsiArrayType _psiArrayType;

    public IntellijJavaArrayType(Module module, PsiArrayType psiArrayType) {
        _module = module;
        _psiArrayType = psiArrayType;
    }

    @Override
    public String getName() {
        return _psiArrayType.getPresentableText();
    }

    @Override
    @NotNull
    public Object getUnderlyingObject() {
        return _psiArrayType;
    }

    @Override
    public IJavaType getComponentType() {
        return IdeaUtils.createJavaTypeFromPsiType(_module, _psiArrayType.getComponentType());
    }
}
