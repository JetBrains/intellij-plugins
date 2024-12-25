// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.build;

import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsTypedModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlexResourceBuildTargetType extends ModuleBasedBuildTargetType<FlexResourceBuildTarget> {
  public static final FlexResourceBuildTargetType PRODUCTION = new FlexResourceBuildTargetType("flex-resource-production", false);
  public static final FlexResourceBuildTargetType TEST = new FlexResourceBuildTargetType("flex-resource-test", true);

  private final boolean myIsTests;

  private FlexResourceBuildTargetType(final String typeId, boolean isTests) {
    super(typeId);
    myIsTests = isTests;
  }

  public boolean isTests() {
    return myIsTests;
  }

  @Override
  public @NotNull List<FlexResourceBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    final List<FlexResourceBuildTarget> targets = new ArrayList<>();
    for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : model.getProject().getModules(JpsFlexModuleType.INSTANCE)) {
      targets.add(new FlexResourceBuildTarget(this, module));
    }
    return targets;
  }

  @Override
  public @NotNull BuildTargetLoader<FlexResourceBuildTarget> createLoader(@NotNull JpsModel model) {
    final Map<String, JpsTypedModule<JpsFlexBuildConfigurationManager>> modules =
      new HashMap<>();
    for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : model.getProject().getModules(JpsFlexModuleType.INSTANCE)) {
      modules.put(module.getName(), module);
    }

    return new BuildTargetLoader<FlexResourceBuildTarget>() {
      @Override
      public @Nullable FlexResourceBuildTarget createTarget(@NotNull String targetId) {
        final JpsTypedModule<JpsFlexBuildConfigurationManager> module = modules.get(targetId);
        return module != null ? new FlexResourceBuildTarget(FlexResourceBuildTargetType.this, module) : null;
      }
    };
  }
}
