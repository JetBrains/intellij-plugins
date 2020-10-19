package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author irengrig
 */
public class PerforcePhysicalConnectionParameters implements PerforcePhysicalConnectionParametersI {
  private final String myPathToExecute;
  private final Project myProject;
  private final int myServerTimeout;
  private final String myCharsetName;

  public PerforcePhysicalConnectionParameters(String pathToExecute, Project project, int serverTimeout, String charsetName) {
    myPathToExecute = pathToExecute;
    myProject = project;
    myServerTimeout = serverTimeout;
    myCharsetName = charsetName;
  }

  @Override
  public String getPathToExec() {
    return myPathToExecute;
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

  @NotNull
  @Override
  public String getCharsetName() {
    return myCharsetName;
  }
}
