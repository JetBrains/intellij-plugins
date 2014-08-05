package org.jetbrains.jps.osmorc.model;

import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.impl.OsmorcJarContentEntry;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author michael.golubev
 */
public interface JpsOsmorcModuleExtension extends JpsElement {
  @NotNull
  JpsModule getModule();

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

  @Nullable
  File findFileInModuleContentRoots(String relativePath);

  @NotNull
  List<OsmorcJarContentEntry> getAdditionalJARContents();

  @NotNull
  String getIgnoreFilePattern();

  boolean isIgnorePatternValid();

  @NotNull
  String getBundlorFileLocation();

  void processAffectedModules(Consumer<JpsModule> consumer);
}
