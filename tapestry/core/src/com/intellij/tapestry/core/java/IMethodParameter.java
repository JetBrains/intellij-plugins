package com.intellij.tapestry.core.java;

/**
 * Represents a JAVA method parameter.
 */
public interface IMethodParameter {

    /**
     * Returns the name of the parameter.
     *
     * @return the name of the parameter.
     */
    String getName();

    /**
     * Returns the type of the parameter.
     *
     * @return the type of the parameter.
     */
    IJavaType getType();
}
