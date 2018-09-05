package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.tapestry.core.java.*;
import com.intellij.tapestry.intellij.util.IdeaUtils;

import java.util.ArrayList;
import java.util.Collection;

public class IntellijJavaMethod implements IJavaMethod {

    private final Module _module;
    private final PsiMethod _psiMethod;

    public IntellijJavaMethod(Module module, PsiMethod psiMethod) {
        _module = module;
        _psiMethod = psiMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _psiMethod.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaType getReturnType() {
        return IdeaUtils.createJavaTypeFromPsiType(_module, _psiMethod.getReturnType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IMethodParameter> getParameters() {
        Collection<IMethodParameter> parameters = new ArrayList<>();

        for (PsiParameter parameter : _psiMethod.getParameterList().getParameters())
            parameters.add(new IntellijMethodParameter(_module, parameter));

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IJavaAnnotation> getAnnotations() {
        Collection<IJavaAnnotation> annotations = new ArrayList<>();

        for (PsiAnnotation annotation : _psiMethod.getModifierList().getAnnotations())
            annotations.add(new IntellijJavaAnnotation(annotation));

        return annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaAnnotation getAnnotation(String annotationQualifiedName) {
        for (IJavaAnnotation annotation : getAnnotations())
            if (annotation.getFullyQualifiedName().equals(annotationQualifiedName)) {
                return annotation;
            }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaClassType getContainingClass() {
        return new IntellijJavaClassType(_module, _psiMethod.getContainingClass().getContainingFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDocumentation() {
        StringBuilder description = new StringBuilder();

        PsiDocComment document = _psiMethod.getDocComment();
        if (document == null) {
            document = ((PsiMethod) _psiMethod.getNavigationElement()).getDocComment();
        }

        if (document != null) {
            for (PsiElement comment : document.getDescriptionElements())
                if (!(comment instanceof PsiWhiteSpace)) {
                    description.append(comment.getText());
                }
        }

        return description.toString();
    }

    public PsiMethod getPsiMethod() {
        return _psiMethod;
    }
}
