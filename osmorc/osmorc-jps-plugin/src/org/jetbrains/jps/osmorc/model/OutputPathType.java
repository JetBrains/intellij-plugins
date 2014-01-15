package org.jetbrains.jps.osmorc.model;

/**
 * The type of output path, where to put the resulting jar.
 */
public enum OutputPathType {
  /**
   * Use the default compiler output path.
   */
  CompilerOutputPath,
  /**
   * Use the OSGi output path specified in the facet settings.
   */
  OsgiOutputPath,
  /**
   * Use a specific output path for this facet.
   */
  SpecificOutputPath
}
