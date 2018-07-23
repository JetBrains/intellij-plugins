package com.intellij.tapestry.core.resource;

import java.io.File;

/**
 * Represents a resource in the application web context.
 */
public interface IResource {

  IResource[] EMPTY_ARRAY = new IResource[0];

  /**
   * @return the resource file name.
   */
  String getName();

  /**
   * @return the file behind this resource.
   */
  File getFile();

  /**
   * @return the file extension without the '.'.
   */
  String getExtension();

  /**
   * Starts the visitor pattern execution.
   * If this resource is not a XML file then this should do nothing.
   *
   * @param visitor the visitor.
   */
  void accept(CoreXmlRecursiveElementVisitor visitor);
}
