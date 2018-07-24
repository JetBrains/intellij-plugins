package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for easy creation of IJavaAnnotation mocks.
 */
public class JavaAnnotationMock implements IJavaAnnotation {

    private String _fullyQualifiedName;
    private final Map<String, String[]> _parameters = new HashMap<>();

    public JavaAnnotationMock() {
    }

    public JavaAnnotationMock(String fullyQualifiedName) {
        _fullyQualifiedName = fullyQualifiedName;
    }

    @Override
    public String getFullyQualifiedName() {
        return _fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        _fullyQualifiedName = fullyQualifiedName;
    }

    @Override
    public Map<String, String[]> getParameters() {
        return _parameters;
    }

    public JavaAnnotationMock addParameter(String name, String[] values) {
        _parameters.put(name, values);

        return this;
    }

    public JavaAnnotationMock addParameter(String name, String value) {
        _parameters.put(name, new String[]{value});

        return this;
    }
}
