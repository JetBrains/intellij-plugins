package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.profiler.ProfileView;
import com.jetbrains.profiler.ProfilerSnapshotProvider;
import org.jetbrains.annotations.NotNull;

/**
 * User: Maxim
 * Date: 21.09.2010
 * Time: 22:38:55
 */
public class ActionScriptProfileProvider implements ProfilerSnapshotProvider {
  static final String ACTIONSCRIPT_SNAPSHOT = "actionscript.snapshot";

  public boolean accepts(@NotNull VirtualFile file) {
    return file.getName().equals(ACTIONSCRIPT_SNAPSHOT);
  }

  public ProfileView createView(@NotNull VirtualFile file, @NotNull Project project) {
    return new ActionScriptProfileView(file, project);
  }
}
