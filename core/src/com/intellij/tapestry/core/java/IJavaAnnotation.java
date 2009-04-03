package com.intellij.tapestry.core.java;

import java.util.Map;

public interface IJavaAnnotation {

    /**
     * @return the fully qualified name of the annotation.
     */
    String getFullyQualifiedName();

    /**
     * @return the annotation parameters.
     */
    Map<String, String[]> getParameters();
}
