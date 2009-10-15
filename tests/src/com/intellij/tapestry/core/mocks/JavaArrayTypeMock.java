package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaType;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public Object getUnderlyingObject() {
        return _name;
    }

    public IJavaType getComponentType() {
        return null;
    }
}
