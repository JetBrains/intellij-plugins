// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

public class DartWebdevParameters implements Cloneable {
  private @NotNull String myHtmlFilePath = "";
  private int myWebdevPort = DartConfigurable.WEBDEV_PORT_DEFAULT;

  public @NotNull String getHtmlFilePath() {
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

  public @NotNull VirtualFile getHtmlFile() throws RuntimeConfigurationError {
    if (StringUtil.isEmptyOrSpaces(myHtmlFilePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.html.file.not.set"));
    }

    final VirtualFile htmlFile = LocalFileSystem.getInstance().findFileByPath(myHtmlFilePath);
    if (htmlFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("html.file.not.found", FileUtil.toSystemDependentName(myHtmlFilePath)));
    }

    if (!FileTypeRegistry.getInstance().isFileOfType(htmlFile, HtmlFileType.INSTANCE)) {
      throw new RuntimeConfigurationError(DartBundle.message("not.a.html.file", FileUtil.toSystemDependentName(myHtmlFilePath)));
    }

    return htmlFile;
  }

  public @NotNull VirtualFile getWorkingDirectory(final @NotNull Project project) throws RuntimeConfigurationError {
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

  public void check(final @NotNull Project project) throws RuntimeConfigurationError {
    final DartSdk dartSdk = DartSdk.getDartSdk(project);
    if (dartSdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"),
                                          () -> DartConfigurable.openDartSettings(project));
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
