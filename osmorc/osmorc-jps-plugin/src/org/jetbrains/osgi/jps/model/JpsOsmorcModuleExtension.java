// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author michael.golubev
 */
public interface JpsOsmorcModuleExtension extends JpsElement {
  JpsElementChildRole<JpsOsmorcModuleExtension> ROLE = JpsElementChildRoleBase.create("Osmorc");

  @NotNull
  String getJarFileLocation();

  @Nullable
  File getBundleDescriptorFile();

  boolean isUseBndFile();

  boolean isUseBndMavenPlugin();

  boolean isUseBundlorFile();

  boolean isManifestManuallyEdited();

  boolean isOsmorcControlsManifest();

  @NotNull
  String getBndFileLocation();

  @NotNull
  Map<String, String> getAdditionalProperties();

  @NotNull
  String getBundleSymbolicName();

  @NotNull
  String getBundleVersion();

  @Nullable
  String getBundleActivator();

  @Nullable
  File getManifestFile();

  @NotNull
  String getManifestLocation();

  boolean isAlwaysRebuildBundleJar();

  @NotNull
  List<OsmorcJarContentEntry> getAdditionalJarContents();

  @Nullable
  String getIgnoreFilePattern();

  @NotNull
  String getBundlorFileLocation();
}
