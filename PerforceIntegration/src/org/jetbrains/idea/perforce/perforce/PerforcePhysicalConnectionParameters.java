package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PerforcePhysicalConnectionParameters implements PerforcePhysicalConnectionParametersI {
  private final String myPathToExecute;
  private final String myPathToIgnore;
  private final Project myProject;
  private final int myServerTimeout;
  private final String myCharsetName;

  public PerforcePhysicalConnectionParameters(String pathToExecute, String pathToIgnore, Project project, int serverTimeout, String charsetName) {
    myPathToExecute = pathToExecute;
    myPathToIgnore = pathToIgnore;
    myProject = project;
    myServerTimeout = serverTimeout;
    myCharsetName = charsetName;
  }

  @Override
  public String getPathToExec() {
    return myPathToExecute;
  }

  @Override
  public String getPathToIgnore() {
    return myPathToIgnore;
  }

  @Override
  public Project getProject() {
    return myProject;
  }

  @Override
  public void disable() {
  }

  @Override
  public int getServerTimeout() {
    return myServerTimeout;
  }

  @Override
  public @NotNull String getCharsetName() {
    return myCharsetName;
  }
}
