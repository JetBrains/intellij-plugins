/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.IOException;
import java.util.Collections;

/**
 * @author michael.golubev
 */
public class OsmorcBuilder extends TargetBuilder<BuildRootDescriptor, OsmorcBuildTarget> {
  public static final String ID = "osgi";

  public OsmorcBuilder() {
    super(Collections.singletonList(OsmorcBuildTargetType.INSTANCE));
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return ID;
  }

  @Override
  public void build(@NotNull OsmorcBuildTarget target,
                    @NotNull DirtyFilesHolder<BuildRootDescriptor, OsmorcBuildTarget> holder,
                    @NotNull BuildOutputConsumer outputConsumer,
                    @NotNull CompileContext context) throws ProjectBuildException, IOException {
    if (target.getExtension().isAlwaysRebuildBundleJar() ||
        JavaBuilderUtil.isForcedRecompilationAllJavaModules(context) ||
        holder.hasDirtyFiles() || holder.hasRemovedFiles()) {
      new OsgiBuildSession().build(target, context);
    }
  }
}
