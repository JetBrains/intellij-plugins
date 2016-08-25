package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
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

public class DartProblemsFilter extends RowFilter<DartProblemsTableModel, Integer> {

  public enum FileFilterMode {All, ContentRoot, Package, Directory, File}

  private static final boolean SHOW_ERRORS_DEFAULT = true;
  private static final boolean SHOW_WARNINGS_DEFAULT = true;
  private static final boolean SHOW_HINTS_DEFAULT = true;
  private static final FileFilterMode FILE_FILTER_MODE_DEFAULT = FileFilterMode.All;

  @NotNull private final Project myProject;

  private boolean myShowErrors;
  private boolean myShowWarnings;
  private boolean myShowHints;
  private FileFilterMode myFileFilterMode;

  @Nullable private VirtualFile myCurrentFile;
  private boolean myPackageRootUpToDate = false;
  @Nullable private VirtualFile myCurrentPackageRoot;
  private boolean myContentRootUpToDate = false;
  @Nullable private VirtualFile myCurrentContentRoot;

  public DartProblemsFilter(@NotNull final Project project) {
    myProject = project;
    resetAllFilters();
  }

  public void resetAllFilters() {
    myShowErrors = SHOW_ERRORS_DEFAULT;
    myShowWarnings = SHOW_WARNINGS_DEFAULT;
    myShowHints = SHOW_HINTS_DEFAULT;
    myFileFilterMode = FILE_FILTER_MODE_DEFAULT;

    assert (!areFiltersApplied());
  }

  public void updateFromUI(@NotNull final DartProblemsFilterForm form) {
    myShowErrors = form.isShowErrors();
    myShowWarnings = form.isShowWarnings();
    myShowHints = form.isShowHints();
    myFileFilterMode = form.getFileFilterMode();
  }

  @SuppressWarnings("PointlessBooleanExpression")
  public boolean areFiltersApplied() {
    if (myShowErrors != SHOW_ERRORS_DEFAULT) return true;
    if (myShowWarnings != SHOW_WARNINGS_DEFAULT) return true;
    if (myShowHints != SHOW_HINTS_DEFAULT) return true;
    if (myFileFilterMode != FILE_FILTER_MODE_DEFAULT) return true;

    return false;
  }

  public boolean setCurrentFile(@Nullable final VirtualFile file) {
    if (Comparing.equal(myCurrentFile, file)) {
      return false;
    }
    else {
      myCurrentFile = file;
      myPackageRootUpToDate = false;
      myContentRootUpToDate = false;
      return true;
    }
  }

  public boolean isShowErrors() {
    return myShowErrors;
  }

  public boolean isShowWarnings() {
    return myShowWarnings;
  }

  public boolean isShowHints() {
    return myShowHints;
  }

  public FileFilterMode getFileFilterMode() {
    return myFileFilterMode;
  }

  @Override
  public boolean include(@NotNull final Entry<? extends DartProblemsTableModel, ? extends Integer> entry) {
    return include(entry.getModel().getItem(entry.getIdentifier()));
  }

  public boolean include(@NotNull final DartProblem problem) {
    if (!myShowErrors && AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) return false;
    if (!myShowWarnings && AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) return false;
    if (!myShowHints && AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) return false;

    if (myFileFilterMode == FileFilterMode.File && (myCurrentFile == null || !myCurrentFile.equals(problem.getFile()))) {
      return false;
    }

    if (myFileFilterMode == FileFilterMode.Directory) {
      if (myCurrentFile == null) return false;

      VirtualFile parent = myCurrentFile.getParent();
      VirtualFile child = problem.getFile();
      if (child != null && parent != null && !child.getPath().startsWith(parent.getPath() + "/")) {
        return false;
      }
    }

    if (myFileFilterMode == FileFilterMode.Package) {
      ensurePackageRootUpToDate();
      if (myCurrentPackageRoot == null || !myCurrentPackageRoot.equals(problem.getPackageRoot())) {
        return false;
      }
    }

    if (myFileFilterMode == FileFilterMode.ContentRoot) {
      ensureContentRootUpToDate();
      if (myCurrentContentRoot == null || !myCurrentContentRoot.equals(problem.getContentRoot())) {
        return false;
      }
    }

    return true;
  }

  private void ensurePackageRootUpToDate() {
    if (myPackageRootUpToDate) return;

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
        final VirtualFile contentRoot =
          ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(myCurrentFile, false);
        packageRoot = contentRoot == null ? null : contentRoot;
      }
      else {
        packageRoot = pubspec.getParent();
      }
    }

    myCurrentPackageRoot = packageRoot;
    myPackageRootUpToDate = true;
  }

  private void ensureContentRootUpToDate() {
    if (myContentRootUpToDate) return;

    myCurrentContentRoot = myCurrentFile == null
                           ? null
                           : ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(myCurrentFile, false);
    myContentRootUpToDate = true;
  }
}
