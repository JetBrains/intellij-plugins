package com.intellij.tapestry.core.java;

/**
 * A Java array type.
 */
public interface IJavaArrayType extends IJavaType {

    /**
     * Returns the component type of the array.
     *
     * @return the component type instance.
     */
    IJavaType getComponentType();
}
