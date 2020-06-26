// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Nullable
  public static JpsOsmorcProjectExtension getExtension(@NotNull JpsProject project) {
    return project.getContainer().getChild(JpsOsmorcProjectExtension.ROLE);
  }

  @Nullable
  public static JpsOsmorcModuleExtension getExtension(@NotNull JpsModule module) {
    return module.getContainer().getChild(JpsOsmorcModuleExtension.ROLE);
  }
}
