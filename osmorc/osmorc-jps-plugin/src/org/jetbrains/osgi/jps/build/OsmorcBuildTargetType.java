// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author michael.golubev
 */
public final class OsmorcBuildTargetType extends ModuleBasedBuildTargetType<OsmorcBuildTarget> {
  public static final OsmorcBuildTargetType INSTANCE = new OsmorcBuildTargetType();

  private OsmorcBuildTargetType() {
    super("osmorc");
  }

  @NotNull
  @Override
  public List<OsmorcBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    List<OsmorcBuildTarget> targets = new ArrayList<>();
    for (JpsModule module : model.getProject().getModules()) {
      JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(module);
      if (extension != null) {
        targets.add(new OsmorcBuildTarget(extension, module));
      }
    }
    return targets;
  }

  @NotNull
  @Override
  public BuildTargetLoader<OsmorcBuildTarget> createLoader(@NotNull JpsModel model) {
    return new Loader(model);
  }

  private static class Loader extends BuildTargetLoader<OsmorcBuildTarget> {
    private final Map<String, OsmorcBuildTarget> myTargets;

    Loader(JpsModel model) {
      myTargets = new HashMap<>();
      for (JpsModule module : model.getProject().getModules()) {
        JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(module);
        if (extension != null) {
          myTargets.put(module.getName(), new OsmorcBuildTarget(extension, module));
        }
      }
    }

    @Nullable
    @Override
    public OsmorcBuildTarget createTarget(@NotNull String targetId) {
      return myTargets.get(targetId);
    }
  }
}
