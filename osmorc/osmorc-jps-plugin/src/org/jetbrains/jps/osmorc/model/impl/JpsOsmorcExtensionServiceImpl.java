package org.jetbrains.jps.osmorc.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsFrameworkInstanceDefinition;
import org.jetbrains.jps.osmorc.model.JpsLibraryBundlificationRule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;

import java.util.List;

/**
 * @author michael.golubev
 */
public class JpsOsmorcExtensionServiceImpl extends JpsOsmorcExtensionService {

  private OsmorcGlobalExtensionProperties myGlobalProperties;

  @Nullable
  @Override
  public JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module) {
    return module.getContainer().getChild(JpsOsmorcModuleExtensionImpl.ROLE);
  }

  public void setGlobalProperties(@NotNull OsmorcGlobalExtensionProperties globalProperties) {
    myGlobalProperties = globalProperties;
  }

  @Override
  public List<JpsFrameworkInstanceDefinition> getFrameworkInstanceDefinitions() {
    return myGlobalProperties.myFrameworkInstanceDefinitions;
  }

  @Override
  public List<JpsLibraryBundlificationRule> getLibraryBundlificationRules() {
    return myGlobalProperties.myLibraryBundlificationRules;
  }
}
