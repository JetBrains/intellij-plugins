// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartProblemsPresentationHelper {

  private final @NotNull Project myProject;
  private @NotNull DartProblemsViewSettings mySettings;

  private @Nullable VirtualFile myCurrentFile;
  private boolean myDartPackageRootUpToDate;
  private @Nullable VirtualFile myCurrentDartPackageRoot;
  private boolean myContentRootUpToDate;
  private @Nullable VirtualFile myCurrentContentRoot;

  DartProblemsPresentationHelper(@NotNull Project project) {
    myProject = project;
    mySettings = new DartProblemsViewSettings(); // Start with default settings. Once actual settings are loaded `setSettings()` is called.
  }

  public void setSettings(@NotNull DartProblemsViewSettings settings) {
    mySettings = settings;
  }

  public @NotNull DartProblemsViewSettings getSettings() {
    return mySettings;
  }

  RowFilter<DartProblemsTableModel, Integer> getRowFilter() {
    return new RowFilter<>() {
      @Override
      public boolean include(final @NotNull Entry<? extends DartProblemsTableModel, ? extends Integer> entry) {
        return shouldShowProblem(entry.getModel().getItem(entry.getIdentifier()));
      }
    };
  }

  void resetAllFilters() {
    mySettings.showErrors = DartProblemsViewSettings.SHOW_ERRORS_DEFAULT;
    mySettings.showWarnings = DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT;
    mySettings.showHints = DartProblemsViewSettings.SHOW_HINTS_DEFAULT;
    mySettings.fileFilterMode = DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT;

    assert !areFiltersApplied();
  }

  void updateFromFilterSettingsUI(@NotNull DartProblemsFilterForm form) {
    mySettings.showErrors = form.isShowErrors();
    mySettings.showWarnings = form.isShowWarnings();
    mySettings.showHints = form.isShowHints();
    mySettings.fileFilterMode = form.getFileFilterMode();
  }

  void updateFromServerSettingsUI(@NotNull DartAnalysisServerSettingsForm form) {
    mySettings.scopedAnalysisMode = form.getScopeAnalysisMode();
  }

  boolean areFiltersApplied() {
    if (mySettings.showErrors != DartProblemsViewSettings.SHOW_ERRORS_DEFAULT) return true;
    if (mySettings.showWarnings != DartProblemsViewSettings.SHOW_WARNINGS_DEFAULT) return true;
    if (mySettings.showHints != DartProblemsViewSettings.SHOW_HINTS_DEFAULT) return true;
    if (mySettings.fileFilterMode != DartProblemsViewSettings.FILE_FILTER_MODE_DEFAULT) return true;

    return false;
  }

  boolean setCurrentFile(@Nullable VirtualFile file) {
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

  boolean isAutoScrollToSource() {
    return mySettings.autoScrollToSource;
  }

  void setAutoScrollToSource(final boolean autoScroll) {
    mySettings.autoScrollToSource = autoScroll;
  }

  boolean isGroupBySeverity() {
    return mySettings.groupBySeverity;
  }

  void setGroupBySeverity(final boolean groupBySeverity) {
    mySettings.groupBySeverity = groupBySeverity;
  }

  boolean isShowErrors() {
    return mySettings.showErrors;
  }

  boolean isShowWarnings() {
    return mySettings.showWarnings;
  }

  boolean isShowHints() {
    return mySettings.showHints;
  }

  DartProblemsViewSettings.FileFilterMode getFileFilterMode() {
    return mySettings.fileFilterMode;
  }

  DartProblemsViewSettings.ScopedAnalysisMode getScopedAnalysisMode() {
    return mySettings.scopedAnalysisMode;
  }

  @Nullable VirtualFile getCurrentFile() {
    return myCurrentFile;
  }

  boolean shouldShowProblem(@NotNull DartProblem problem) {
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

  public @NotNull @Nls String getFilterTypeText() {
    final String filtering = switch (getFileFilterMode()) {
      case ContentRoot -> DartBundle.message("dart.problems.view.filter.current.content.root");
      case DartPackage -> DartBundle.message("dart.problems.view.filter.current.dart.package");
      case Directory -> DartBundle.message("dart.problems.view.filter.current.directory");
      case File -> DartBundle.message("dart.problems.view.filter.current.file");
      default -> "";
    };

    boolean filteringBySeverity = !isShowErrors() || !isShowWarnings() || !isShowHints();

    if (!filteringBySeverity) {
      return DartBundle.message("dart.problems.view.filtering.by.0", filtering);
    }

    if (filtering.isEmpty()) {
      return DartBundle.message("dart.problems.view.filtering.by.severity");
    }

    return DartBundle.message("dart.problems.view.filtering.by.0.and.severity", filtering);
  }
}
