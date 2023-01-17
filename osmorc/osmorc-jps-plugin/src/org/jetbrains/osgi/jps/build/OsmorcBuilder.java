// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.IOException;
import java.util.Collections;

/**
 * @author michael.golubev
 */
public class OsmorcBuilder extends TargetBuilder<BuildRootDescriptor, OsmorcBuildTarget> {
  public static final @NlsSafe String ID = "osgi";

  public OsmorcBuilder() {
    super(Collections.singletonList(OsmorcBuildTargetType.INSTANCE));
  }

  @Override
  public @NotNull String getPresentableName() {
    return ID;
  }

  @Override
  public void build(@NotNull OsmorcBuildTarget target,
                    @NotNull DirtyFilesHolder<BuildRootDescriptor, OsmorcBuildTarget> holder,
                    @NotNull BuildOutputConsumer outputConsumer,
                    @NotNull CompileContext context) throws IOException {
    if (target.getExtension().isAlwaysRebuildBundleJar() ||
        JavaBuilderUtil.isForcedRecompilationAllJavaModules(context) ||
        holder.hasDirtyFiles() || holder.hasRemovedFiles()) {
      new OsgiBuildSession().build(target, context);
    }
  }
}
