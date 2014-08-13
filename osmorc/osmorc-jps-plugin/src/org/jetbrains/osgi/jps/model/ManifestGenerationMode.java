package org.jetbrains.osgi.jps.model;

public enum ManifestGenerationMode {
  /**
   * No generation at all. All is manual.
   */
  Manually,
  /**
   * Osmorc will generate it using facet settings.
   */
  OsmorcControlled,
  /**
   * Bnd will generate it using a bnd file.
   */
  Bnd,
  /**
   * Bundlor will generate it, using a bundlor file.
   */
  Bundlor
}
