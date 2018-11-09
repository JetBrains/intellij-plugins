package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.java.IMethodParameter;

/**
 * Utility class for easy creation of IMethodParameter mocks.
 */
public class MethodParameterMock implements IMethodParameter {
    private final String _name;
    private final IJavaType _type;

    public MethodParameterMock(String name, IJavaType type) {
        _name = name;
        _type = type;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public IJavaType getType() {
        return _type;
    }
}
