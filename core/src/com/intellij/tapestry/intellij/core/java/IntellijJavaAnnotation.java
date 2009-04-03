package com.intellij.tapestry.intellij.core.java;

import com.intellij.psi.*;
import com.intellij.tapestry.core.java.IJavaAnnotation;

import java.util.HashMap;
import java.util.Map;

public class IntellijJavaAnnotation implements IJavaAnnotation {

    private PsiAnnotation _psiAnnotation;

    public IntellijJavaAnnotation(PsiAnnotation psiAnnotation) {
        _psiAnnotation = psiAnnotation;
    }

    /**
     * {@inheritDoc}
     */
    public String getFullyQualifiedName() {
        return _psiAnnotation.getQualifiedName();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String[]> getParameters() {
        Map<String, String[]> parameters = new HashMap<String, String[]>();

        for (PsiNameValuePair parameter : _psiAnnotation.getParameterList().getAttributes()) {
            PsiAnnotationMemberValue value = parameter.getValue();

            if (value instanceof PsiLiteralExpression && ((PsiLiteralExpression) value).getValue() != null)
                parameters.put(parameter.getName(), new String[]{((PsiLiteralExpression) value).getValue().toString()});
            if (value instanceof PsiArrayInitializerMemberValue) {
                String[] values = new String[((PsiArrayInitializerMemberValue) value).getInitializers().length];
                for (int i = 0; i < ((PsiArrayInitializerMemberValue) value).getInitializers().length; i++) {
                    if (((PsiLiteralExpression) ((PsiArrayInitializerMemberValue) value).getInitializers()[i]).getValue() != null)
                        values[i] = ((PsiLiteralExpression) ((PsiArrayInitializerMemberValue) value).getInitializers()[i]).getValue().toString();
                }

                parameters.put(parameter.getName(), values);
            }
        }

        return parameters;
    }

    public PsiAnnotation getPsiAnnotation() {
        return _psiAnnotation;
    }
}
