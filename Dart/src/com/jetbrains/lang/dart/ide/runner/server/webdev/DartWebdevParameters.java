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
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartWebdevParameters implements Cloneable {
  @Nullable
  private String myHTMLFilePath = null;

  @Nullable
  public String getHTMLFilePath() {
    return myHTMLFilePath;
  }

  public void setHTMLFilePath(final @Nullable String htmlFilePath) {
    myHTMLFilePath = htmlFilePath;
  }

  @NotNull
  public VirtualFile getHtmlFile() throws RuntimeConfigurationError {
    if (StringUtil.isEmptyOrSpaces(myHTMLFilePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.html.file.not.set"));
    }

    final VirtualFile htmlFile = LocalFileSystem.getInstance().findFileByPath(myHTMLFilePath);
    if (htmlFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("html.file.not.found", FileUtil.toSystemDependentName(myHTMLFilePath)));
    }

    if (htmlFile.getFileType() != HtmlFileType.INSTANCE) {
      throw new RuntimeConfigurationError(DartBundle.message("not.a.html.file", FileUtil.toSystemDependentName(myHTMLFilePath)));
    }

    return htmlFile;
  }

  @NotNull
  public String computeProcessWorkingDirectory(@NotNull final Project project) {
    try {
      return DartCommandLineRunnerParameters.suggestDartWorkingDir(project, getHtmlFile());
    }
    catch (RuntimeConfigurationError error) {
      return "";
    }
  }

  public void check(final @NotNull Project project) throws RuntimeConfigurationError {
    // check sdk
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"),
                                          () -> DartConfigurable.openDartSettings(project));
    }

    // TODO(jwren) check that the Dart SDK is at or past some Dart SDK version that has the webdev support

    // check main html file
    if (StringUtil.isEmptyOrSpaces(myHTMLFilePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.html.file.not.set"));
    }

    // TODO(jwren) check that the html file is named "index.html"
    // TODO(jwren) check that the html file is in a directory named "web"
    // TODO(jwren) other verifications, such as the existence of a main.dart file, yaml file, .packages file
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
