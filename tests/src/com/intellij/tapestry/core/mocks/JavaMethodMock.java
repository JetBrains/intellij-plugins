package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class for easy creation of IJavaMethod mocks.
 */
public class JavaMethodMock implements IJavaMethod {
    private final String _name;
    private IJavaType _returnType;
    private Collection<IMethodParameter> _parameters = new ArrayList<>();
    private final Collection<IJavaAnnotation> _annotations = new ArrayList<>();
    private IJavaClassType _containingClass;
    private String _documentation;

    public JavaMethodMock(String name) {
        _name = name;
    }

    public JavaMethodMock(String name, IJavaType returnType) {
        _name = name;
        _returnType = returnType;
    }

    public JavaMethodMock(String name, IJavaType returnType, Collection<IMethodParameter> parameters) {
        _name = name;
        _returnType = returnType;
        _parameters = parameters;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public IJavaType getReturnType() {
        return _returnType;
    }

    @Override
    public Collection<IMethodParameter> getParameters() {
        return _parameters;
    }

    public JavaMethodMock addParameter(IMethodParameter parameter) {
        _parameters.add(parameter);

        return this;
    }

    @Override
    public Collection<IJavaAnnotation> getAnnotations() {
        return _annotations;
    }

    public void addAnnotation(IJavaAnnotation annotation) {
        _annotations.add(annotation);
    }

    @Override
    public IJavaAnnotation getAnnotation(String annotationQualifiedName) {
        for (IJavaAnnotation annotation : _annotations)
            if (annotation.getFullyQualifiedName().equals(annotationQualifiedName))
                return annotation;

        return null;
    }

    @Override
    public IJavaClassType getContainingClass() {
        return _containingClass;
    }

    public void setContainingClass(IJavaClassType containingClass) {
        _containingClass = containingClass;
    }

    @Override
    public String getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }
}
