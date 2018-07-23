package com.intellij.tapestry.core.java;

import java.util.Map;

/**
 * Represents a JAVa field.
 */
public interface IJavaField {

    /**
     * @return the name of the method.
     */
    String getName();

    /**
     * @return the type of the field.
     */
    IJavaType getType();

    /**
     * @return {@code true} if the type is public, {@code false} otherwise.
     */
    boolean isPrivate();

    /**
     * @return the annotations of the type.
     */
    Map<String, IJavaAnnotation> getAnnotations();

    /**
     * @return the javadoc description of the field.
     */
    String getDocumentation();

    /**
     * Returns the string representation of the declaration of this field.
     *
     * @return the string representation of the declaration of this field.
     */
    String getStringRepresentation();

    /**
     * @return {@code true} if the field is valid, {@code false} otherwise.
     */
    boolean isValid();
}
