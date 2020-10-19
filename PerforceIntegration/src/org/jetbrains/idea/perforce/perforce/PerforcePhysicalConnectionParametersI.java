package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author irengrig
 */
public interface PerforcePhysicalConnectionParametersI {
  String getPathToExec();
  Project getProject();
  void disable();

  int getServerTimeout();
  @NotNull String getCharsetName();
}
