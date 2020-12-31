/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
