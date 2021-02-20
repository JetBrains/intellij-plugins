// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.DiagnosticMessage;
import org.dartlang.analysis.server.protocol.Location;
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

  public DartProblem(@NotNull Project project, @NotNull AnalysisError error) {
    myProject = project;
    myAnalysisError = error;
  }

  public @NotNull String getErrorMessage() {
    return myAnalysisError.getMessage();
  }

  public @Nullable String getCorrectionMessage() {
    return StringUtil.notNullize(myAnalysisError.getCorrection());
  }

  public @Nullable String getUrl() {
    return myAnalysisError.getUrl();
  }

  public @Nullable List<DiagnosticMessage> getContextMessages() {
    return myAnalysisError.getContextMessages();
  }

  public @NotNull String getCode() {
    return StringUtil.notNullize(myAnalysisError.getCode());
  }

  public String getSeverity() {
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
      mySystemIndependentPath = FileUtil.toSystemIndependentName(myAnalysisError.getLocation().getFile());
    }
    return mySystemIndependentPath;
  }

  private void ensureInitialized() {
    if (myPresentableLocationWithoutLineNumber != null) return;

    // temporary final vars guarantee that vars are initialized before this method exits
    final VirtualFile file;
    final String dartPackageName;
    final String presentableFilePath;
    final VirtualFile packageRoot;
    final VirtualFile contentRoot;

    file = LocalFileSystem.getInstance().findFileByPath(getSystemIndependentPath());
    if (file == null) {
      dartPackageName = null;
      packageRoot = null;
      contentRoot = null;
      presentableFilePath = myAnalysisError.getLocation().getFile();
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
          presentableFilePath = myAnalysisError.getLocation().getFile();
        }
        else {
          packageRoot = contentRoot;
          final String relativePath = VfsUtilCore.getRelativePath(file, contentRoot, File.separatorChar);
          presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
        }
      }
      else {
        final String projectName = Registry.is("dart.projects.without.pubspec", false)
                                   ? DartBuildFileUtil.getDartProjectName(pubspec)
                                   : PubspecYamlUtil.getDartProjectName(pubspec);
        dartPackageName = projectName != null ? projectName : "%unnamed%";
        packageRoot = pubspec.getParent();
        final String relativePath = VfsUtilCore.getRelativePath(file, pubspec.getParent(), File.separatorChar);
        presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
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
    ensureInitialized();
    return myPresentableLocationWithoutLineNumber;
  }

  public @NotNull String getPresentableLocation() {
    return getPresentableLocationWithoutLineNumber() + ":" + getLineNumber();
  }

  public @Nullable VirtualFile getFile() {
    ensureInitialized();
    return myFile;
  }

  public @Nullable VirtualFile getPackageRoot() {
    ensureInitialized();
    return myPackageRoot;
  }

  public @Nullable VirtualFile getContentRoot() {
    ensureInitialized();
    return myContentRoot;
  }

  public static @NotNull String generateTooltipText(@NotNull String message,
                                                    @Nullable List<DiagnosticMessage> contextMessages,
                                                    @Nullable String correction,
                                                    @Nullable String url) {
    StringBuilder tooltip = new StringBuilder("<html>").append(XmlStringUtil.escapeString(message));

    // Include the URL link to documentation.
    if (StringUtil.isNotEmpty(url)) {
      tooltip.append(" (<a href='").append(url).append("'>Documentation</a>)");
    }

    // For each context message, include the message and location.
    if (contextMessages != null && !contextMessages.isEmpty()) {
      tooltip.append("<ul>");
      for (DiagnosticMessage contextMessage : contextMessages) {
        Location location = contextMessage.getLocation();
        String msg = XmlStringUtil.escapeString(StringUtil.trimEnd(contextMessage.getMessage(), '.'));
        String locationText = XmlStringUtil.escapeString(PathUtil.getFileName(location.getFile()) + ":" + location.getStartLine());
        tooltip.append("<li>").append(msg).append(" (").append(locationText).append(").</li>");
      }
      tooltip.append("</ul>");
    }
    else if (StringUtil.isNotEmpty(correction)) {
      tooltip.append("<br/><br/>");
    }

    // Include the correction text, if included.
    if (StringUtil.isNotEmpty(correction)) {
      tooltip.append(XmlStringUtil.escapeString(correction));
    }

    tooltip.append("</html>");
    return tooltip.toString();
  }
}
