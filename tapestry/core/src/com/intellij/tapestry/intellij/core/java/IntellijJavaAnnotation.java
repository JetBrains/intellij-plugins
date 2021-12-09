package com.intellij.tapestry.intellij.core.java;

import com.intellij.psi.*;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class IntellijJavaAnnotation implements IJavaAnnotation {
    private final PsiAnnotation _psiAnnotation;

    public IntellijJavaAnnotation(PsiAnnotation psiAnnotation) {
        _psiAnnotation = psiAnnotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullyQualifiedName() {
        return _psiAnnotation.getQualifiedName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getParameters() {
        return CachedValuesManager.getProjectPsiDependentCache(_psiAnnotation, IntellijJavaAnnotation::doCalcParameters);
    }

    public PsiAnnotation getPsiAnnotation() {
        return _psiAnnotation;
    }

    private static Map<String, String[]> doCalcParameters(PsiAnnotation owner) {
        Map<String, String[]> parameters = new HashMap<>(); // HashMap to handle null keys

        for (PsiNameValuePair parameter : owner.getParameterList().getAttributes()) {
            String literalValue = parameter.getLiteralValue();
            if (literalValue != null) {
                parameters.put(parameter.getName(), new String[] {literalValue});
                continue;
            }

            PsiAnnotationMemberValue value = parameter.getValue();
            String stringValue = calcValue(value);

            if (stringValue != null) {
                parameters.put(parameter.getName(), new String[]{stringValue});
            } else if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue)value).getInitializers();
                String[] values = new String[initializers.length];
                for (int i = 0; i < initializers.length; i++) {
                    values[i] = calcValue (initializers[i]);
                }

                parameters.put(parameter.getName(), values);
            }
        }

        return parameters;
    }

    private static @Nullable String calcValue(PsiAnnotationMemberValue value) {
        if (value instanceof PsiLiteralExpression && ((PsiLiteralExpression) value).getValue() != null)
            return ((PsiLiteralExpression) value).getValue().toString();
        if (value instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression)value).resolve();
            if (resolve instanceof PsiField) {
                PsiExpression initializer = ((PsiField)resolve).getInitializer();
                if (initializer instanceof PsiLiteralExpression) {
                    return ((PsiLiteralExpression) initializer).getValue().toString();
                }
            }
        }
        return null;
    }
}
