package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaPrimitiveType;
import com.intellij.tapestry.core.java.IJavaType;

/**
 * Utility class for easy creation of JavaPrimitiveType mocks.
 */
public class JavaPrimitiveTypeMock implements IJavaPrimitiveType {
    private String _name;

    public JavaPrimitiveTypeMock(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public boolean isAssignableFrom(IJavaType type) {
        return false;
    }

    public Object getUnderlyingObject() {
        return null;
    }
}
