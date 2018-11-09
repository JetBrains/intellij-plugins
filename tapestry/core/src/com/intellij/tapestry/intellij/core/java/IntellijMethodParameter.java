package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.java.IMethodParameter;

public class IntellijMethodParameter implements IMethodParameter {

    private final Module _module;
    private final PsiParameter _psiParameter;

    public IntellijMethodParameter(Module module, PsiParameter psiParameter) {
        _module = module;
        _psiParameter = psiParameter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _psiParameter.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaType getType() {
        if (_psiParameter.getType() instanceof PsiClassType) {
            return new IntellijJavaClassType(_module, ((PsiClassType) _psiParameter.getType()).resolve().getContainingFile());
        }

        if (_psiParameter.getType() instanceof PsiPrimitiveType) {
            return new IntellijJavaPrimitiveType((PsiPrimitiveType) _psiParameter.getType());
        }

        return null;
    }
}
