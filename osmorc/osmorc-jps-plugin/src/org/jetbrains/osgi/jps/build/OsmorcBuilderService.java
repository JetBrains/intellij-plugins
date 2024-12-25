// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.Collections;
import java.util.List;

public class OsmorcBuilderService extends BuilderService {
  @Override
  public @NotNull List<? extends BuildTargetType<?>> getTargetTypes() {
    return Collections.singletonList(OsmorcBuildTargetType.INSTANCE);
  }

  @Override
  public @NotNull List<? extends TargetBuilder<?, ?>> createBuilders() {
    return Collections.singletonList(new OsmorcBuilder());
  }
}
