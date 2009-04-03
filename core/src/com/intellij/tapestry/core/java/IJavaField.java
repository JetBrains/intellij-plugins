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
     * @return <code>true</code> if the type is public, <code>false</false> otherwise.
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
     * @return <code>true</code> if the field is valid, <code>false</false> otherwise.
     */
    boolean isValid();
}
