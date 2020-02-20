// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.analysis.problemsView.AnalysisProblem;
import com.intellij.analysis.problemsView.AnalysisProblemsPresentationHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartProblemsPresentationHelper extends AnalysisProblemsPresentationHelper {
  @NotNull private final Project myProject;
  @NotNull private DartProblemsViewSettings mySettings;

  @Nullable private VirtualFile myCurrentFile;
  private boolean myDartPackageRootUpToDate;
  @Nullable private VirtualFile myCurrentDartPackageRoot;
  private boolean myContentRootUpToDate;
  @Nullable private VirtualFile myCurrentContentRoot;

  DartProblemsPresentationHelper(@NotNull final Project project) {
    myProject = project;
    mySettings = new DartProblemsViewSettings(); // Start with default settings. Once actual settings are loaded `setSettings()` is called.
  }

  public void setSettings(@NotNull final DartProblemsViewSettings settings) {
    mySettings = settings;
  }

  @NotNull
  public DartProblemsViewSettings getSettings() {
    return mySettings;
  }

  @Override
  public void resetAllFilters() {
    mySettings.showErrors = DartProblemsViewSettings.SHOW_ERRORS_DEFAULT;
    mySettings.showWarnings = DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT;
    mySettings.showHints = DartProblemsViewSettings.SHOW_ERRORS_DEFAULT;
    mySettings.fileFilterMode = DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT;

    assert !areFiltersApplied();
  }

  void updateFromFilterSettingsUI(@NotNull final DartProblemsFilterForm form) {
    mySettings.showErrors = form.isShowErrors();
    mySettings.showWarnings = form.isShowWarnings();
    mySettings.showHints = form.isShowHints();
    mySettings.fileFilterMode = form.getFileFilterMode();
  }

  void updateFromServerSettingsUI(@NotNull final DartAnalysisServerSettingsForm form) {
    mySettings.scopedAnalysisMode = form.getScopeAnalysisMode();
  }

  @Override
  public boolean areFiltersApplied() {
    if (mySettings.showErrors != DartProblemsViewSettings.SHOW_ERRORS_DEFAULT) return true;
    if (mySettings.showWarnings != DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT) return true;
    if (mySettings.showHints != DartProblemsViewSettings.SHOW_HINTS_DEFAULT) return true;
    if (mySettings.fileFilterMode != DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT) return true;

    return false;
  }

  boolean setCurrentFile(@Nullable final VirtualFile file) {
    if (Comparing.equal(myCurrentFile, file)) {
      return false;
    }
    else {
      myCurrentFile = file;
      myDartPackageRootUpToDate = false;
      myContentRootUpToDate = false;
      return true;
    }
  }

  @Override
  public boolean isAutoScrollToSource() {
    return mySettings.autoScrollToSource;
  }

  @Override
  public void setAutoScrollToSource(final boolean autoScroll) {
    mySettings.autoScrollToSource = autoScroll;
  }

  @Override
  public boolean isGroupBySeverity() {
    return mySettings.groupBySeverity;
  }

  @Override
  public void setGroupBySeverity(final boolean groupBySeverity) {
    mySettings.groupBySeverity = groupBySeverity;
  }

  @Override
  public boolean isShowErrors() {
    return mySettings.showErrors;
  }

  @Override
  public boolean isShowWarnings() {
    return mySettings.showWarnings;
  }

  @Override
  public boolean isShowHints() {
    return mySettings.showHints;
  }

  @NotNull
  DartProblemsViewSettings.FileFilterMode getFileFilterMode() {
    return mySettings.fileFilterMode;
  }

  @NotNull
  DartProblemsViewSettings.ScopedAnalysisMode getScopedAnalysisMode() {
    return mySettings.scopedAnalysisMode;
  }

  @Override
  @Nullable
  public VirtualFile getCurrentFile() {
    return myCurrentFile;
  }

  @Override
  public boolean shouldShowProblem(@NotNull final AnalysisProblem problem) {
    if (!isShowErrors() && AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) return false;
    if (!isShowWarnings() && AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) return false;
    if (!isShowHints() && AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) return false;

    if (getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.File &&
        (myCurrentFile == null || !myCurrentFile.equals(problem.getFile()))) {
      return false;
    }

    if (getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.Directory) {
      if (myCurrentFile == null) return false;

      VirtualFile parent = myCurrentFile.getParent();
      VirtualFile child = problem.getFile();
      if (child != null && parent != null && !child.getPath().startsWith(parent.getPath() + "/")) {
        return false;
      }
    }

    if (getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.DartPackage) {
      ensurePackageRootUpToDate();
      if (myCurrentDartPackageRoot == null || !myCurrentDartPackageRoot.equals(problem.getPackageRoot())) {
        return false;
      }
    }

    if (getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.ContentRoot) {
      ensureContentRootUpToDate();
      if (myCurrentContentRoot == null || !myCurrentContentRoot.equals(problem.getContentRoot())) {
        return false;
      }
    }

    return true;
  }

  private void ensurePackageRootUpToDate() {
    if (myDartPackageRootUpToDate) return;

    // temp var to make sure that value is initialized
    final VirtualFile packageRoot;

    if (myCurrentFile == null) {
      packageRoot = null;
    }
    else {
      final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                  ? DartBuildFileUtil.findPackageRootBuildFile(myProject, myCurrentFile)
                                  : PubspecYamlUtil.findPubspecYamlFile(myProject, myCurrentFile);
      if (pubspec == null) {
        packageRoot = ProjectFileIndex.getInstance(myProject).getContentRootForFile(myCurrentFile, false);
      }
      else {
        packageRoot = pubspec.getParent();
      }
    }

    myCurrentDartPackageRoot = packageRoot;
    myDartPackageRootUpToDate = true;
  }

  private void ensureContentRootUpToDate() {
    if (myContentRootUpToDate) return;

    myCurrentContentRoot = myCurrentFile == null
                           ? null
                           : ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(myCurrentFile, false);
    myContentRootUpToDate = true;
  }

  @Override
  @NotNull
  public String getFilterTypeText() {
    final StringBuilder builder = new StringBuilder();

    switch (getFileFilterMode()) {
      case All:
        break;
      case ContentRoot:
        builder.append("filtering by current content root");
        break;
      case DartPackage:
        builder.append("filtering by current Dart package");
        break;
      case Directory:
        builder.append("filtering by current directory");
        break;
      case File:
        builder.append("filtering by current file");
        break;
    }

    if (!isShowErrors() || !isShowWarnings() || !isShowHints()) {
      builder.append(builder.length() == 0 ? "filtering by severity" : " and severity");
    }

    return builder.toString();
  }
}
