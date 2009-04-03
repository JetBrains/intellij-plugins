package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaType;

/**
 * Utility class for easy creation of IJavaArrayType mocks.
 */
public class JavaArrayTypeMock implements IJavaArrayType {
    private String _name;

    public JavaArrayTypeMock(String name) {
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

    public IJavaType getComponentType() {
        return null;
    }
}
