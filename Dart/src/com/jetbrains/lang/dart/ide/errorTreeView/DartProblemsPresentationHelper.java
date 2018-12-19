// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

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

import javax.swing.*;

public class DartProblemsPresentationHelper {

  @NotNull private final Project myProject;
  @NotNull DartProblemsViewSettings mySettings;

  @Nullable private VirtualFile myCurrentFile;
  private boolean myDartPackageRootUpToDate = false;
  @Nullable private VirtualFile myCurrentDartPackageRoot;
  private boolean myContentRootUpToDate = false;
  @Nullable private VirtualFile myCurrentContentRoot;

  public DartProblemsPresentationHelper(@NotNull final Project project) {
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

  public RowFilter<DartProblemsTableModel, Integer> getRowFilter() {
    return new RowFilter<DartProblemsTableModel, Integer>() {
      @Override
      public boolean include(@NotNull final Entry<? extends DartProblemsTableModel, ? extends Integer> entry) {
        return shouldShowProblem(entry.getModel().getItem(entry.getIdentifier()));
      }
    };
  }

  public void resetAllFilters() {
    mySettings.showErrors = DartProblemsViewSettings.SHOW_ERRORS_DEFAULT;
    mySettings.showWarnings = DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT;
    mySettings.showHints = DartProblemsViewSettings.SHOW_ERRORS_DEFAULT;
    mySettings.fileFilterMode = DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT;

    assert (!areFiltersApplied());
  }

  public void updateFromFilterSettingsUI(@NotNull final DartProblemsFilterForm form) {
    mySettings.showErrors = form.isShowErrors();
    mySettings.showWarnings = form.isShowWarnings();
    mySettings.showHints = form.isShowHints();
    mySettings.fileFilterMode = form.getFileFilterMode();
  }

  public void updateFromServerSettingsUI(@NotNull final DartAnalysisServerSettingsForm form) {
    mySettings.scopedAnalysisMode = form.getScopeAnalysisMode();
  }

  public boolean areFiltersApplied() {
    if (mySettings.showErrors != DartProblemsViewSettings.SHOW_ERRORS_DEFAULT) return true;
    if (mySettings.showWarnings != DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT) return true;
    if (mySettings.showHints != DartProblemsViewSettings.SHOW_HINTS_DEFAULT) return true;
    if (mySettings.fileFilterMode != DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT) return true;

    return false;
  }

  public boolean setCurrentFile(@Nullable final VirtualFile file) {
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

  public boolean isAutoScrollToSource() {
    return mySettings.autoScrollToSource;
  }

  public void setAutoScrollToSource(final boolean autoScroll) {
    mySettings.autoScrollToSource = autoScroll;
  }

  public boolean isGroupBySeverity() {
    return mySettings.groupBySeverity;
  }

  public void setGroupBySeverity(final boolean groupBySeverity) {
    mySettings.groupBySeverity = groupBySeverity;
  }

  public boolean isShowErrors() {
    return mySettings.showErrors;
  }

  public boolean isShowWarnings() {
    return mySettings.showWarnings;
  }

  public boolean isShowHints() {
    return mySettings.showHints;
  }

  public DartProblemsViewSettings.FileFilterMode getFileFilterMode() {
    return mySettings.fileFilterMode;
  }

  DartProblemsViewSettings.ScopedAnalysisMode getScopedAnalysisMode() {
    return mySettings.scopedAnalysisMode;
  }

  @Nullable
  VirtualFile getCurrentFile() {
    return myCurrentFile;
  }

  public boolean shouldShowProblem(@NotNull final DartProblem problem) {
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
}
