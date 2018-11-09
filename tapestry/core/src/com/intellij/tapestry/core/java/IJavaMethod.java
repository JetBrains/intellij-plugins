package com.intellij.tapestry.core.java;

import java.util.Collection;

/**
 * Represents a JAVA method.
 */
public interface IJavaMethod {

    /**
     * @return the name of the method.
     */
    String getName();

    /**
     * @return the return type of the method.
     */
    IJavaType getReturnType();

    /**
     * @return the parameters of the method.
     */
    Collection<IMethodParameter> getParameters();

    /**
     * @return the annotations of the method.
     */
    Collection<IJavaAnnotation> getAnnotations();

    /**
     * @param annotationQualifiedName the qualified name of the annotation for look for.
     * @return the annotation of the method with the given qualified name.
     */
    IJavaAnnotation getAnnotation(String annotationQualifiedName);

    /**
     * @return the class that contains this method.
     */
    IJavaClassType getContainingClass();

    /**
     * @return the javadoc description of the method.
     */
    String getDocumentation();
}
