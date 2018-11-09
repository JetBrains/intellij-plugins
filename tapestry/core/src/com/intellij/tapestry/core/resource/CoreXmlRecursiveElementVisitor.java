package com.intellij.tapestry.core.resource;

import com.intellij.tapestry.core.resource.xml.XmlTag;

/**
 * A visitor for XML files.
 */
public interface CoreXmlRecursiveElementVisitor {

    void visitTag(XmlTag tag);
}
