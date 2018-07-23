package com.intellij.tapestry.core.model.presentation.valueresolvers;

import com.intellij.tapestry.core.java.IJavaType;

/**
 * The resolved value.
 */
public class ResolvedValue {

    private final IJavaType _type;
    private final Object _codeBind;

    public ResolvedValue(IJavaType type, Object codeBind) {
        _type = type;
        _codeBind = codeBind;
    }

    public IJavaType getType() {
        return _type;
    }

    public Object getCodeBind() {
        return _codeBind;
    }
}
