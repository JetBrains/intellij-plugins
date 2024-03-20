// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.*;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.DiagnosticMessage;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class DartProblem {

  private final @NotNull Project myProject;
  private final @NotNull AnalysisError myAnalysisError;

  private String mySystemIndependentPath;

  private @Nullable VirtualFile myFile;
  private @Nullable VirtualFile myPackageRoot;
  private @Nullable VirtualFile myContentRoot;
  private String myPresentableLocationWithoutLineNumber;

  @RequiresBackgroundThread
  @RequiresReadLock
  public DartProblem(@NotNull Project project, @NotNull AnalysisError error) {
    myProject = project;
    myAnalysisError = error;
    initialize();
  }

  public @NotNull @NlsSafe String getErrorMessage() {
    return myAnalysisError.getMessage();
  }

  public @Nullable @NlsSafe String getCorrectionMessage() {
    return StringUtil.notNullize(myAnalysisError.getCorrection());
  }

  public @Nullable @NonNls String getUrl() {
    return myAnalysisError.getUrl();
  }

  public @Nullable List<DiagnosticMessage> getContextMessages() {
    return myAnalysisError.getContextMessages();
  }

  public @NotNull @NonNls String getCode() {
    return StringUtil.notNullize(myAnalysisError.getCode());
  }

  public @NotNull @NonNls String getSeverity() {
    return myAnalysisError.getSeverity();
  }

  public int getLineNumber() {
    return myAnalysisError.getLocation().getStartLine();
  }

  public int getOffset() {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(mySystemIndependentPath);
    return DartAnalysisServerService.getInstance(myProject).getConvertedOffset(file, myAnalysisError.getLocation().getOffset());
  }

  public @NotNull String getSystemIndependentPath() {
    if (mySystemIndependentPath == null) {
      String filePathOrUri = myAnalysisError.getLocation().getFile();
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
      mySystemIndependentPath = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.getFilePath()
                                                                                    : ((DartNotLocalFileInfo)fileInfo).getFileUri();
    }
    return mySystemIndependentPath;
  }

  @RequiresBackgroundThread
  @RequiresReadLock
  private void initialize() {
    if (myPresentableLocationWithoutLineNumber != null) return;

    // temporary final vars guarantee that vars are initialized before this method exits
    final VirtualFile file;
    final String dartPackageName;
    final String presentableFilePath;
    final VirtualFile packageRoot;
    final VirtualFile contentRoot;

    String filePathOrUri = myAnalysisError.getLocation().getFile();
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
    file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

    if (file == null) {
      dartPackageName = null;
      packageRoot = null;
      contentRoot = null;
      presentableFilePath = filePathOrUri;
    }
    else {
      contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(file, false);

      final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                  ? DartBuildFileUtil.findPackageRootBuildFile(myProject, file)
                                  : PubspecYamlUtil.findPubspecYamlFile(myProject, file);
      if (pubspec == null) {
        dartPackageName = null;
        if (contentRoot == null) {
          packageRoot = null;
          presentableFilePath = filePathOrUri;
        }
        else {
          packageRoot = contentRoot;
          final String relativePath = VfsUtilCore.getRelativePath(file, contentRoot, File.separatorChar);
          presentableFilePath = relativePath != null ? relativePath : filePathOrUri;
        }
      }
      else {
        final String projectName = Registry.is("dart.projects.without.pubspec", false)
                                   ? DartBuildFileUtil.getDartProjectName(pubspec)
                                   : PubspecYamlUtil.getDartProjectName(pubspec);
        dartPackageName = projectName != null ? projectName : "%unnamed%";
        packageRoot = pubspec.getParent();
        final String relativePath = VfsUtilCore.getRelativePath(file, pubspec.getParent(), File.separatorChar);
        presentableFilePath = relativePath != null ? relativePath : filePathOrUri;
      }
    }

    myFile = file;
    myPackageRoot = packageRoot;
    myContentRoot = contentRoot;
    myPresentableLocationWithoutLineNumber = dartPackageName == null ? presentableFilePath
                                                                     : ("[" + dartPackageName + "] " + presentableFilePath);
  }


  /**
   * Returns Dart package name in brackets and relative path form Dart package root to the file.
   * If no pubspec.yaml then returns relative part from content root to the file.
   * File path is returned as failover.
   */
  public @NotNull String getPresentableLocationWithoutLineNumber() {
    return myPresentableLocationWithoutLineNumber;
  }

  public @NotNull String getPresentableLocation() {
    return getPresentableLocationWithoutLineNumber() + ":" + getLineNumber();
  }

  public @Nullable VirtualFile getFile() {
    return myFile;
  }

  public @Nullable VirtualFile getPackageRoot() {
    return myPackageRoot;
  }

  public @Nullable VirtualFile getContentRoot() {
    return myContentRoot;
  }

  public static @NotNull @Nls String generateTooltipText(@NotNull @Nls String message,
                                                         @Nullable List<DiagnosticMessage> contextMessages,
                                                         @Nullable @Nls String correction,
                                                         @Nullable @NonNls String url) {
    // First include the error message.
    HtmlBuilder htmlBuilder = new HtmlBuilder().append(message);

    // Include the URL link to documentation.
    if (StringUtil.isNotEmpty(url)) {
      htmlBuilder.append(" (").appendLink(url, DartBundle.message("error.doc.link")).append(")");
    }

    htmlBuilder.append(HtmlChunk.br());

    // For each context message, include the message and location.
    if (contextMessages != null && !contextMessages.isEmpty()) {
      HtmlBuilder listBuilder = new HtmlBuilder();
      for (DiagnosticMessage contextMessage : contextMessages) {
        Location location = contextMessage.getLocation();
        @NlsSafe String msg = StringUtil.trimEnd(contextMessage.getMessage(), '.');
        String filePathOrUri = location.getFile();
        String locationText = PathUtil.getFileName(filePathOrUri) + ":" + location.getStartLine();
        listBuilder.append(HtmlChunk.li().addText(msg + " (" + locationText + ")."));
      }
      htmlBuilder.append(listBuilder.wrapWith("ul"));
    }

    // Include the correction text, if included.
    if (StringUtil.isNotEmpty(correction)) {
      htmlBuilder.append(HtmlChunk.br()).append(correction);
    }

    return htmlBuilder.wrapWith("html").toString();
  }
}
