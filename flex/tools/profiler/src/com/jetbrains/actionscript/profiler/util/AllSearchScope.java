package com.jetbrains.actionscript.profiler.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.EverythingGlobalScope;
import com.jetbrains.actionscript.profiler.ProfilerBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author Fedor.Korotkov
 */
public class AllSearchScope extends EverythingGlobalScope {
  public AllSearchScope(Project project) {
    super(project);
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return ProfilerBundle.message("all.scope.name");
  }
}
