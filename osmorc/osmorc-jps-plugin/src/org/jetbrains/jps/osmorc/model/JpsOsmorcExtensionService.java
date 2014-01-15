package org.jetbrains.jps.osmorc.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.service.JpsServiceManager;

/**
 * @author michael.golubev
 */
public abstract class JpsOsmorcExtensionService {

  public static JpsOsmorcExtensionService getInstance() {
    return JpsServiceManager.getInstance().getService(JpsOsmorcExtensionService.class);
  }

  @Nullable
  public abstract JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module);
}
