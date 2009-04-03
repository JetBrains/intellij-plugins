package com.intellij.tapestry.core.model.externalizable;

import com.intellij.tapestry.core.java.IJavaClassType;

/**
 * Every class that implements this has a representation that can be included in a class.
 */
public interface ExternalizableToClass {

    String getClassRepresentation(IJavaClassType targetClass) throws Exception;
}
