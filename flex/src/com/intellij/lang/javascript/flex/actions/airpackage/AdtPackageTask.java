package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

import java.io.File;

public abstract class AdtPackageTask extends AdtTask {

  private long myStartTime;
  private final String myPackageFilePath;

  public AdtPackageTask(final Project project, final Sdk flexSdk, final String packageFilePath) {
    super(project, flexSdk);
    myPackageFilePath = packageFilePath;
  }

  public void start() {
    myStartTime = System.currentTimeMillis();
    super.start();
  }

  protected boolean checkMessages() {
    // in this way we distinguish between errors and warnings
    return new File(myPackageFilePath).lastModified() > myStartTime;
  }
}
