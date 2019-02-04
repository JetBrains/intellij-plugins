package com.intellij.tapestry.core.java;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a JAVA type.
 */
public interface IJavaType {

    /**
     * Returns the type name.
     *
     * @return the type name.
     */
    String getName();

    /**
     * Tests whether a given type can be converted to the type
     * represented by this object.
     *
     * @param type the type object to be checked
     * @return the {@code boolean} value indicating whether objects of the
     *         type {@code type} can be assigned to objects of this class
     */
    boolean isAssignableFrom(@Nullable IJavaType type);

    /**
     * @return returns the underlying object of this class. This is usually an IDE specific object.
     */
    @NotNull
    Object getUnderlyingObject();
}
