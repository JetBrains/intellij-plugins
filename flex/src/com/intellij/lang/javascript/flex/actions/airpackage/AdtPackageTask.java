// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.PathUtil;
import com.intellij.util.net.ProxyUtils;

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
    var command = super.createCommandLine();
    var proxySettings = ProxyUtils.getCurrentSettingsAsJvmProperties();

    int i = 1; // after java executable
    for (var proxySetting : proxySettings.entrySet()) {
      command.add(i++, "-D" + proxySetting.getKey() + "=" + proxySetting.getValue());
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
