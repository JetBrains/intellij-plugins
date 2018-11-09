package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class IntellijJavaField implements IJavaField {

    private final Module _module;
    private final PsiField _psiField;

    public IntellijJavaField(Module module, PsiField psiField) {
        _module = module;
        _psiField = psiField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _psiField.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IJavaType getType() {
        if (_psiField.getType() instanceof PsiClassType) {
            return IdeaUtils.createJavaTypeFromPsiType(_module, _psiField.getType());
        }

        if (_psiField.getType() instanceof PsiPrimitiveType) {
            return IdeaUtils.createJavaTypeFromPsiType(_module, _psiField.getType());
        }

        if (_psiField.getType() instanceof PsiArrayType)
            return IdeaUtils.createJavaTypeFromPsiType(_module, _psiField.getType());

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivate() {
        return _psiField.getModifierList().hasModifierProperty(PsiModifier.PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, IJavaAnnotation> getAnnotations() {
        Map<String, IJavaAnnotation> annotations = new HashMap<>();

        for (PsiAnnotation annotation : _psiField.getModifierList().getAnnotations())
            if (annotation.isValid())
                annotations.put(annotation.getQualifiedName(), new IntellijJavaAnnotation(annotation));

        return annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDocumentation() {
        StringBuilder description = new StringBuilder();

        PsiDocComment document = _psiField.getDocComment();
        if (document == null) {
            document = ((PsiField) _psiField.getNavigationElement()).getDocComment();
        }

        if (document != null) {
            for (PsiElement comment : document.getDescriptionElements())
                if (!(comment instanceof PsiWhiteSpace)) {
                    description.append(comment.getText());
                }
        }

        return description.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringRepresentation() {
        return _psiField.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return _psiField.isValid();
    }

    public PsiField getPsiField() {
        return _psiField;
    }

    public boolean equals(Object obj) {
        return obj instanceof IntellijJavaField && getName().equals(((IntellijJavaField) obj).getName());
    }

    public int hashCode() {
        return getName().hashCode();
    }
}