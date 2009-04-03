package com.intellij.tapestry.core.model.externalizable;

/**
 * Every class that implements this has a representation that can be included in a template.
 */
public interface ExternalizableToTemplate {

    String getTemplateRepresentation(String namespacePrefix) throws Exception;
}
