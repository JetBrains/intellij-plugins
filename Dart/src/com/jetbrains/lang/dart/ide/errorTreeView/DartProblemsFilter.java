package com.jetbrains.lang.dart.ide.errorTreeView;

import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartProblemsFilter extends RowFilter<DartProblemsTableModel, Integer> {

  public enum FileFilterMode {All, Package, File}


  private static final boolean SHOW_ERRORS_DEFAULT = true;
  private static final boolean SHOW_WARNINGS_DEFAULT = true;
  private static final boolean SHOW_HINTS_DEFAULT = true;
  private static final FileFilterMode FILE_FILTER_MODE_DEFAULT = FileFilterMode.All;


  private boolean myShowErrors;
  private boolean myShowWarnings;
  private boolean myShowHints;
  private FileFilterMode myFileFilterMode;

  public DartProblemsFilter() {
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
  public boolean include(Entry<? extends DartProblemsTableModel, ? extends Integer> entry) {
    final DartProblem problem = entry.getModel().getItem(entry.getIdentifier());
    if (!myShowErrors && AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) return false;
    if (!myShowWarnings && AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) return false;
    if (!myShowHints && AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) return false;

    return true;
  }
}
