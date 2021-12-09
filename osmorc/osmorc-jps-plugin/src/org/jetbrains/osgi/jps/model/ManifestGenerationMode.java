// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
   * Bnd will generate it using a bnd file, configured from bnd-maven-plugin.
   */
  BndMavenPlugin,
  /**
   * Bundlor will generate it, using a bundlor file.
   */
  Bundlor
}
