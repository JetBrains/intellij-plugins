// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.analysis.problemsView.AnalysisErrorSeverity;
import com.intellij.analysis.problemsView.AnalysisProblem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.DiagnosticMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DartProblem implements AnalysisProblem {

  @NotNull private final Project myProject;
  @NotNull private final AnalysisError myAnalysisError;

  private String mySystemIndependentPath;

  @Nullable private VirtualFile myFile;
  @Nullable private VirtualFile myPackageRoot;
  @Nullable private VirtualFile myContentRoot;
  private String myPresentableLocationWithoutLineNumber;

  public DartProblem(@NotNull final Project project, @NotNull final AnalysisError error) {
    myProject = project;
    myAnalysisError = error;
  }

  @Override
  @NotNull
  public String getErrorMessage() {
    return myAnalysisError.getMessage();
  }

  @Override
  @Nullable
  public String getCorrectionMessage() {
    return StringUtil.notNullize(myAnalysisError.getCorrection());
  }

  @Override
  @Nullable
  public String getUrl() {
    return myAnalysisError.getUrl();
  }

  @Override
  public @NotNull List<AnalysisProblem> getSecondaryMessages() {
    return ContainerUtil.map(ObjectUtils.notNull(myAnalysisError.getContextMessages(), Collections.emptyList()),
                             dm->new DartProblem(myProject,
                                                 new AnalysisError(AnalysisErrorSeverity.WARNING,
                                                                   "context message",
                                                                   dm.getLocation(),
                                                                   dm.getMessage(),
                                                                   "",
                                                                   "",
                                                                   dm.getLocation() == null ? null : dm.getLocation().getFile() == null ? null : VfsUtilCore.pathToUrl(dm.getLocation().getFile()),
                                                                   null,
                                                                   null)));
  }

  @Nullable List<DiagnosticMessage> getDiagnosticMessages() {
    return myAnalysisError.getContextMessages();
  }

  @Override
  @NotNull
  public String getCode() {
    return StringUtil.notNullize(myAnalysisError.getCode());
  }

  /**
   * @see AnalysisErrorSeverity
   */
  @Override
  public String getSeverity() {
    return myAnalysisError.getSeverity();
  }

  @Override
  public int getLineNumber() {
    return myAnalysisError.getLocation().getStartLine();
  }

  @Override
  public int getOffset() {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(mySystemIndependentPath);
    return DartAnalysisServerService.getInstance(myProject).getConvertedOffset(file, myAnalysisError.getLocation().getOffset());
  }

  @Override
  @NotNull
  public String getSystemIndependentPath() {
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
  @Override
  @NotNull
  public String getPresentableLocationWithoutLineNumber() {
    ensureInitialized();
    return myPresentableLocationWithoutLineNumber;
  }

  @Override
  @NotNull
  public String getPresentableLocation() {
    return getPresentableLocationWithoutLineNumber() + ":" + getLineNumber();
  }

  @Override
  @Nullable
  public VirtualFile getFile() {
    ensureInitialized();
    return myFile;
  }

  @Override
  @Nullable
  public VirtualFile getPackageRoot() {
    ensureInitialized();
    return myPackageRoot;
  }

  @Override
  @Nullable
  public VirtualFile getContentRoot() {
    ensureInitialized();
    return myContentRoot;
  }

  @Override
  @NotNull
  public String getTooltip() {
    // Pass null to the url, mouse movement to the hover makes the tooltip go away, see https://youtrack.jetbrains.com/issue/WEB-39449
    return AnalysisProblem.generateTooltipText(getErrorMessage(), getCorrectionMessage(), null);
  }
}
