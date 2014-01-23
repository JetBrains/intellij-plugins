package org.jetbrains.jps.osmorc.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.impl.OsmorcGlobalExtensionProperties;
import org.jetbrains.jps.service.JpsServiceManager;

import java.util.List;

/**
 * @author michael.golubev
 */
public abstract class JpsOsmorcExtensionService {

  public static JpsOsmorcExtensionService getInstance() {
    return JpsServiceManager.getInstance().getService(JpsOsmorcExtensionService.class);
  }

  @Nullable
  public abstract JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module);

  public abstract void setGlobalProperties(@NotNull OsmorcGlobalExtensionProperties globalProperties);

  public abstract List<JpsFrameworkInstanceDefinition> getFrameworkInstanceDefinitions();

  public abstract List<JpsLibraryBundlificationRule> getLibraryBundlificationRules();
}
