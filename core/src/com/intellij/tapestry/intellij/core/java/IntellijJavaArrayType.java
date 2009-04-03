package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiArrayType;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.intellij.util.IdeaUtils;

public class IntellijJavaArrayType extends IntellijJavaType implements IJavaArrayType {

    private Module _module;
    private PsiArrayType _psiArrayType;

    public IntellijJavaArrayType(Module module, PsiArrayType psiArrayType) {
        _module = module;
        _psiArrayType = psiArrayType;
    }

    public String getName() {
        return _psiArrayType.getPresentableText();
    }

    public Object getUnderlyingObject() {
        return _psiArrayType;
    }

    public IJavaType getComponentType() {
        return IdeaUtils.createJavaTypeFromPsiType(_module, _psiArrayType.getComponentType());
    }
}
