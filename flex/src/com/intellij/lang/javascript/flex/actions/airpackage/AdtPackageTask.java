package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import com.intellij.util.PathUtil;
import com.intellij.util.net.HttpConfigurable;

import java.io.File;
import java.util.List;

public abstract class AdtPackageTask extends AdtTask {

  private long myStartTime;
  private final String myPackageFilePath;

  public AdtPackageTask(final Project project, final Sdk flexSdk, final String packageFilePath) {
    super(project, flexSdk);
    myPackageFilePath = packageFilePath;
  }

  @Override
  protected List<String> createCommandLine() {
    final List<String> command = super.createCommandLine();
    final List<Pair<String, String>> proxySettings = HttpConfigurable.getInstance().getJvmProperties(false, null);

    int i = 1; // after java executable
    for (Pair<String, String> proxySetting : proxySettings) {
      command.add(i++, "-D" + proxySetting.first + "=" + proxySetting.second);
    }
    return command;
  }

  @Override
  protected File getProcessDir() {
    return new File(PathUtil.getParentPath(myPackageFilePath));
  }

  @Override
  public void start() {
    myStartTime = System.currentTimeMillis();
    super.start();
  }

  @Override
  protected boolean checkMessages() {
    // in this way we distinguish between errors and warnings
    return new File(myPackageFilePath).lastModified() > myStartTime;
  }
}
