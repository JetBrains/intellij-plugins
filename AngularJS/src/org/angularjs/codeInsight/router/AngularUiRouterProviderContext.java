package org.angularjs.codeInsight.router;

import com.intellij.openapi.components.ServiceManager;
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
    return ServiceManager.getService(project, AngularUiRouterProviderContext.class);
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
