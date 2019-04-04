/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.jps.build;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;

import java.util.List;
import java.util.Map;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTargetType extends ModuleBasedBuildTargetType<OsmorcBuildTarget> {
  public static final OsmorcBuildTargetType INSTANCE = new OsmorcBuildTargetType();

  private OsmorcBuildTargetType() {
    super("osmorc");
  }

  @NotNull
  @Override
  public List<OsmorcBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    List<OsmorcBuildTarget> targets = ContainerUtil.newArrayList();
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
      myTargets = ContainerUtil.newHashMap();
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
