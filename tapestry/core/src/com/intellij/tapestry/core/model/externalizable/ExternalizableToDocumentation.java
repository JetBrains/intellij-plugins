package com.intellij.tapestry.core.model.externalizable;

/**
 * Every class that implements this has a representation in the form of documentation.
 */
public interface ExternalizableToDocumentation {

    String getDocumentation() throws Exception;
}
