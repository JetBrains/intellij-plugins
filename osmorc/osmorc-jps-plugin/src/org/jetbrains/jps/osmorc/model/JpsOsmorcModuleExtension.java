package org.jetbrains.jps.osmorc.model;

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

  boolean isUseBndFile();

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

  @NotNull
  List<OsmorcJarContentEntry> getAdditionalJarContents();

  @Nullable
  String getIgnoreFilePattern();

  @NotNull
  String getBundlorFileLocation();
}
