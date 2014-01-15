package org.jetbrains.jps.osmorc.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;

/**
 * @author michael.golubev
 */
public class JpsOsmorcExtensionServiceImpl extends JpsOsmorcExtensionService {

  @Nullable
  @Override
  public JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module) {
    return module.getContainer().getChild(JpsOsmorcModuleExtensionImpl.ROLE);
  }
}
