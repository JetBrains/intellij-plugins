// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.router;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Irina.Chernushina on 7/5/2016.
 */
public class AngularUiRouterProviderContext {
  private final @NotNull Project myProject;
  private final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> myData;

  public static AngularUiRouterProviderContext getInstance(@NotNull Project project) {
    return project.getService(AngularUiRouterProviderContext.class);
  }

  public AngularUiRouterProviderContext(@NotNull Project project) {
    myProject = project;
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
