// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartWebdevParameters implements Cloneable {
  private static final String MIN_DART_SDK_VERSION_FOR_WEBDEV = "2.5.0";

  @NotNull private String myHtmlFilePath = "";
  private int myWebdevPort = DartConfigurable.WEBDEV_PORT_DEFAULT;
  @NotNull private String myWorkingDirectory = "";

  @NotNull
  public String getHtmlFilePath() {
    return myHtmlFilePath;
  }

  public void setHtmlFilePath(@NotNull final String htmlFilePath) {
    myHtmlFilePath = htmlFilePath;
  }

  public int getWebdevPort() {
    return myWebdevPort;
  }

  public void setWebdevPort(final int webdevPort) {
    myWebdevPort = webdevPort;
  }

  public void setWorkingDirectory(@NotNull final String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  @Nullable
  public String computeRelativeHtmlFile() {
    if (StringUtil.isEmptyOrSpaces(myHtmlFilePath) || StringUtil.isEmptyOrSpaces(myWorkingDirectory)) {
      return null;
    }
    if (myWorkingDirectory.length() >= myHtmlFilePath.length()) {
      return null;
    }
    if (myWorkingDirectory.endsWith("/")) {
      return myHtmlFilePath.substring(myWorkingDirectory.length());
    } else {
      return myHtmlFilePath.substring(myWorkingDirectory.length() + 1);
    }
  }

  @NotNull
  public VirtualFile getHtmlFile() throws RuntimeConfigurationError {
    if (StringUtil.isEmptyOrSpaces(myHtmlFilePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.html.file.not.set"));
    }

    final VirtualFile htmlFile = LocalFileSystem.getInstance().findFileByPath(myHtmlFilePath);
    if (htmlFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("html.file.not.found", FileUtil.toSystemDependentName(myHtmlFilePath)));
    }

    if (htmlFile.getFileType() != HtmlFileType.INSTANCE) {
      throw new RuntimeConfigurationError(DartBundle.message("not.a.html.file", FileUtil.toSystemDependentName(myHtmlFilePath)));
    }

    return htmlFile;
  }

  public void check(@NotNull final Project project) throws RuntimeConfigurationError {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"),
                                          () -> DartConfigurable.openDartSettings(project));
    }
    final String sdkVersion = sdk.getVersion();
    if (!sdkVersion.isEmpty() && StringUtil.compareVersionNumbers(sdkVersion, MIN_DART_SDK_VERSION_FOR_WEBDEV) < 0) {
      throw new RuntimeConfigurationError(DartBundle.message("old.dart.sdk.for.webdev", MIN_DART_SDK_VERSION_FOR_WEBDEV, sdkVersion));
    }

    // check html file
    getHtmlFile();

    // check working directory
    if (StringUtil.isEmptyOrSpaces(myWorkingDirectory)) {
      throw new RuntimeConfigurationError(DartBundle.message("work.dir.not.set"));
    }
    final VirtualFile workDir = LocalFileSystem.getInstance().findFileByPath(myWorkingDirectory);
    if (workDir == null || !workDir.isDirectory()) {
      throw new RuntimeConfigurationError(
        DartBundle.message("work.dir.does.not.exist", FileUtil.toSystemDependentName(myWorkingDirectory)));
    }

    if (!getHtmlFilePath().contains(myWorkingDirectory)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.html.file.not.in.work.dir"));
    }
  }

  @Override
  public DartWebdevParameters clone() {
    try {
      return (DartWebdevParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
