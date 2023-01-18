package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface PerforcePhysicalConnectionParametersI {
  String getPathToExec();
  String getPathToIgnore();
  Project getProject();
  void disable();

  int getServerTimeout();
  @NotNull String getCharsetName();
}
