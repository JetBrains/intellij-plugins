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

  String getBndFileLocation();

  Map<String, String> getBndFileProperties();

  @Nullable
  File getManifestFile();

  @NotNull
  String getManifestLocation();

  @Nullable
  File findFileInModuleContentRoots(String relativePath);

  @NotNull
  List<OsmorcJarContentEntry> getAdditionalJARContents();

  @NotNull
  Map<String, String> getAdditionalPropertiesAsMap();

  @NotNull
  String getIgnoreFilePattern();

  boolean isIgnorePatternValid();

  @NotNull
  String getBundlorFileLocation();

  void processAffectedModules(Consumer<JpsModule> consumer);
}
