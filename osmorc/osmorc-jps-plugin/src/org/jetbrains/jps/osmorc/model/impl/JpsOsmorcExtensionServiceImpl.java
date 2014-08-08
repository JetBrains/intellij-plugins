package org.jetbrains.jps.osmorc.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.osmorc.model.LibraryBundlificationRule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;

import java.util.List;

/**
 * @author michael.golubev
 */
public class JpsOsmorcExtensionServiceImpl extends JpsOsmorcExtensionService {
  private OsmorcGlobalExtensionProperties myGlobalProperties;

  @Override
  public void setGlobalProperties(@NotNull OsmorcGlobalExtensionProperties globalProperties) {
    myGlobalProperties = globalProperties;
  }

  @NotNull
  @Override
  public List<LibraryBundlificationRule> getLibraryBundlificationRules() {
    return myGlobalProperties.myLibraryBundlificationRules;
  }
}
