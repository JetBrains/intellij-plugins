// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

public class DartWebdevParameters implements Cloneable {
  @NotNull
  private String myHtmlFilePath = "";
  private int myWebdevPort = DartConfigurable.WEBDEV_PORT_DEFAULT;

  @NotNull
  public String getHtmlFilePath() {
    return myHtmlFilePath;
  }

  public void setHtmlFilePath(final @NotNull String htmlFilePath) {
    myHtmlFilePath = htmlFilePath;
  }

  public int getWebdevPort() {
    return myWebdevPort;
  }

  public void setWebdevPort(final int webdevPort) {
    myWebdevPort = webdevPort;
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

  @NotNull
  public VirtualFile getWorkingDirectory(@NotNull final Project project) throws RuntimeConfigurationError {
    VirtualFile htmlFile = getHtmlFile();
    VirtualFile pubspecFile = PubspecYamlUtil.findPubspecYamlFile(project, htmlFile);
    if (pubspecFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("html.file.not.within.dart.project"));
    }
    VirtualFile dartProjectRoot = pubspecFile.getParent();
    if (htmlFile.getParent().equals(dartProjectRoot)) {
      throw new RuntimeConfigurationError(DartBundle.message("html.file.right.in.dart.project.root"));
    }
    return dartProjectRoot;
  }

  public void check(@NotNull final Project project) throws RuntimeConfigurationError {
    final DartSdk dartSdk = DartSdk.getDartSdk(project);
    if (dartSdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"),
                                          () -> DartConfigurable.openDartSettings(project));
    }
    final String dartSdkVersion = dartSdk.getVersion();
    if (!dartSdkVersion.isEmpty() &&
        !DartAnalysisServerService.isDartSdkVersionSufficientForWebdev(dartSdk)) {
      throw new RuntimeConfigurationError(
        DartBundle.message("old.dart.sdk.for.webdev", DartAnalysisServerService.MIN_WEBDEV_SDK_VERSION, dartSdkVersion));
    }

    // check html file
    getHtmlFile();
    getWorkingDirectory(project);
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
