// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagram;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.angularjs.codeInsight.router.AngularUiRouterGraphBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class AngularUiRouterProviderContext {
  private final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> myData;

  public static AngularUiRouterProviderContext getInstance(@NotNull Project project) {
    return project.getService(AngularUiRouterProviderContext.class);
  }

  AngularUiRouterProviderContext(@NotNull Project project) {
    myData = new HashMap<>();
  }

  public void reset() {
    myData.clear();
  }

  public void registerNodesBuilder(AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder) {
    myData.put(nodesBuilder.getKey(), nodesBuilder);
  }

  public @Nullable AngularUiRouterGraphBuilder.GraphNodesBuilder getBuilder(final @NotNull VirtualFile file) {
    return myData.get(file);
  }
}
