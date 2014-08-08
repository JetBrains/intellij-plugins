package org.jetbrains.jps.osmorc.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.impl.OsmorcGlobalExtensionProperties;
import org.jetbrains.jps.service.JpsServiceManager;

import java.util.List;

/**
 * @author michael.golubev
 */
public abstract class JpsOsmorcExtensionService {
  @NotNull
  public static JpsOsmorcExtensionService getInstance() {
    return JpsServiceManager.getInstance().getService(JpsOsmorcExtensionService.class);
  }

  @Nullable
  public static JpsOsmorcProjectExtension getExtension(@NotNull JpsProject project) {
    return project.getContainer().getChild(JpsOsmorcProjectExtension.ROLE);
  }

  @Nullable
  public static JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module) {
    return module.getContainer().getChild(JpsOsmorcModuleExtension.ROLE);
  }

  public abstract void setGlobalProperties(@NotNull OsmorcGlobalExtensionProperties globalProperties);

  @NotNull
  public abstract List<LibraryBundlificationRule> getLibraryBundlificationRules();
}
