package com.intellij.tapestry.core.java;

import org.jetbrains.annotations.NotNull;

/**
 * A type that is assignable to every other type.
 */
public class AssignableToAll implements IJavaType {

    private static final AssignableToAll _me = new AssignableToAll();

    public static AssignableToAll getInstance() {
        return _me;
    }

    @Override
    public String getName() {
        return "assignable";
    }

    @Override
    public boolean isAssignableFrom(IJavaType type) {
        return true;
    }

    @Override
    @NotNull
    public Object getUnderlyingObject() {
        return this;
    }
}
