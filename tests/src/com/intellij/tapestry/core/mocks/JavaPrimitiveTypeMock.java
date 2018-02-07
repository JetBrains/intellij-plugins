package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaPrimitiveType;
import com.intellij.tapestry.core.java.IJavaType;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for easy creation of JavaPrimitiveType mocks.
 */
public class JavaPrimitiveTypeMock implements IJavaPrimitiveType {
    private final String _name;

    public JavaPrimitiveTypeMock(String name) {
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
}
