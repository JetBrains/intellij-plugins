package com.intellij.tapestry.core.java;

/**
 * A type that is assignable to every other type.
 */
public class AssignableToAll implements IJavaType {

    private static final AssignableToAll _me = new AssignableToAll();

    public static AssignableToAll getInstance() {
        return _me;
    }

    public String getName() {
        return "assignable";
    }

    public boolean isAssignableFrom(IJavaType type) {
        return true;
    }

    public Object getUnderlyingObject() {
        return null;
    }
}
