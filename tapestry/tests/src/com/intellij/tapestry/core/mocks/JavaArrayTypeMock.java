package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaType;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for easy creation of IJavaArrayType mocks.
 */
public class JavaArrayTypeMock implements IJavaArrayType {
    private final String _name;

    public JavaArrayTypeMock(String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean isAssignableFrom(IJavaType type) {
        return false;
    }

    @Override
    @NotNull
    public Object getUnderlyingObject() {
        return _name;
    }

    @Override
    public IJavaType getComponentType() {
        return null;
    }
}
