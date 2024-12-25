// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;

/**
 * @author michael.golubev
 */
public final class JpsOsmorcExtensionService {

  private JpsOsmorcExtensionService() { }

  public static @Nullable JpsOsmorcProjectExtension getExtension(@NotNull JpsProject project) {
    return project.getContainer().getChild(JpsOsmorcProjectExtension.ROLE);
  }

  public static @Nullable JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module) {
    return module.getContainer().getChild(JpsOsmorcModuleExtension.ROLE);
  }
}
