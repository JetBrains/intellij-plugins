package com.intellij.tapestry.intellij.core.java;

import com.intellij.psi.PsiPrimitiveType;
import com.intellij.tapestry.core.java.IJavaPrimitiveType;

public class IntellijJavaPrimitiveType extends IntellijJavaType implements IJavaPrimitiveType {

    private PsiPrimitiveType _psiPrimitiveType;

    public IntellijJavaPrimitiveType(PsiPrimitiveType psiPrimitiveType) {
        _psiPrimitiveType = psiPrimitiveType;
    }

    public String getName() {
        return _psiPrimitiveType.getPresentableText();
    }

    public Object getUnderlyingObject() {
        return _psiPrimitiveType;
    }
}
