// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.jetbrains.lang.dart.ide.annotator.DartAnnotator;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.*;

class DartProblemsTableModel extends ListTableModel<DartProblem> {

  private static final TableCellRenderer MESSAGE_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // Do not emphasize focused cell, drawing the whole row as selected is enough
      final JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

      final DartProblem problem = (DartProblem)value;
      setText(problem.getErrorMessage().replaceAll("(\n)+", " "));

      setToolTipText(generateToolTipText(problem.getErrorMessage(), problem.getCorrectionMessage()));

      final String severity = problem.getSeverity();
      setIcon(AnalysisErrorSeverity.ERROR.equals(severity)
              ? AllIcons.General.Error
              : AnalysisErrorSeverity.WARNING.equals(severity)
                ? AllIcons.General.Warning
                : AllIcons.General.Information);

      return label;
    }
  };

  @NotNull
  private static String generateToolTipText(@Nullable final String message, @Nullable final String correction) {
    String messageSanitized = StringUtil.notNullize(message).replaceAll("\\\\n", "\n");
    String correctionSanitized = StringUtil.notNullize(correction).replaceAll("\\\\n", "\n");
    return correctionSanitized.isEmpty() ? messageSanitized : messageSanitized + "\n\n" + correctionSanitized;
  }

  private static final TableCellRenderer LOCATION_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // Do not emphasize focused cell, drawing the whole row as selected is enough
      return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
  };

  private final Project myProject;
  @NotNull private final DartProblemsPresentationHelper myPresentationHelper;

  // Kind of hack to keep a reference to the live collection used in a super class, but it allows to improve performance greatly.
  // Having it in hand we can do bulk rows removal with a single fireTableRowsDeleted() call afterwards
  private final List<DartProblem> myItems;

  private RowSorter.SortKey mySortKey = new RowSorter.SortKey(1, SortOrder.ASCENDING);

  private int myErrorCount = 0;
  private int myWarningCount = 0;
  private int myHintCount = 0;

  private int myErrorCountAfterFilter = 0;
  private int myWarningCountAfterFilter = 0;
  private int myHintCountAfterFilter = 0;

  private final Comparator<DartProblem> myDescriptionComparator = new DartProblemsComparator(DartProblemsComparator.MESSAGE_COLUMN_ID);
  private final Comparator<DartProblem> myLocationComparator = new DartProblemsComparator(DartProblemsComparator.LOCATION_COLUMN_ID);

  DartProblemsTableModel(@NotNull final Project project, @NotNull final DartProblemsPresentationHelper presentationHelper) {
    myProject = project;
    myPresentationHelper = presentationHelper;
    myItems = new ArrayList<>();
    setColumnInfos(new ColumnInfo[]{createDescriptionColumn(), createLocationColumn()});
    setItems(myItems);
    setSortable(true);
  }

  @NotNull
  private ColumnInfo<DartProblem, DartProblem> createDescriptionColumn() {
    return new ColumnInfo<DartProblem, DartProblem>("Description") {
      @Nullable
      @Override
      public Comparator<DartProblem> getComparator() {
        return myDescriptionComparator;
      }

      @Nullable
      @Override
      public TableCellRenderer getRenderer(@NotNull final DartProblem problem) {
        return MESSAGE_RENDERER;
      }

      @NotNull
      @Override
      public DartProblem valueOf(@NotNull final DartProblem problem) {
        return problem;
      }
    };
  }

  @NotNull
  private ColumnInfo<DartProblem, String> createLocationColumn() {
    return new ColumnInfo<DartProblem, String>("Location") {
      @Nullable
      @Override
      public Comparator<DartProblem> getComparator() {
        return myLocationComparator;
      }

      @Nullable
      @Override
      public TableCellRenderer getRenderer(DartProblem problem) {
        return LOCATION_RENDERER;
      }

      @NotNull
      @Override
      public String valueOf(@NotNull final DartProblem problem) {
        return problem.getPresentableLocation();
      }
    };
  }

  @Override
  public RowSorter.SortKey getDefaultSortKey() {
    return mySortKey;
  }

  @Override
  public boolean canExchangeRows(int oldIndex, int newIndex) {
    return false;
  }

  @Override
  public void exchangeRows(int idx1, int idx2) {
    throw new IllegalStateException();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public void removeRows(final int firstRow, final int lastRow) {
    assert lastRow >= firstRow;

    for (int i = lastRow; i >= firstRow; i--) {
      final DartProblem removed = myItems.remove(i);

      if (AnalysisErrorSeverity.ERROR.equals(removed.getSeverity())) myErrorCount--;
      if (AnalysisErrorSeverity.WARNING.equals(removed.getSeverity())) myWarningCount--;
      if (AnalysisErrorSeverity.INFO.equals(removed.getSeverity())) myHintCount--;
      updateProblemsCountAfterFilter(removed, false);
    }

    fireTableRowsDeleted(firstRow, lastRow);
  }

  public void removeAll() {
    final int rowCount = getRowCount();
    if (rowCount > 0) {
      myItems.clear();
      fireTableRowsDeleted(0, rowCount - 1);
    }

    myErrorCount = 0;
    myWarningCount = 0;
    myHintCount = 0;
    myErrorCountAfterFilter = 0;
    myWarningCountAfterFilter = 0;
    myHintCountAfterFilter = 0;
  }

  /**
   * If {@code selectedProblem} was removed and similar one added again then this method returns the added one,
   * so that the caller could update selected row in the table
   */
  @Nullable
  public DartProblem setErrorsAndReturnReplacementForSelection(@NotNull final Map<String, List<AnalysisError>> filePathToErrors,
                                                               @Nullable final DartProblem selectedProblem) {
    final boolean selectedProblemRemoved = removeRowsForFilesInSet(filePathToErrors.keySet(), selectedProblem);
    return addErrorsAndReturnReplacementForSelection(filePathToErrors, selectedProblemRemoved ? selectedProblem : null);
  }

  private boolean removeRowsForFilesInSet(@NotNull final Set<String> filePaths, @Nullable final DartProblem selectedProblem) {
    // Looks for regions in table items that should be removed and removes them.
    // For performance reasons we try to call removeRows() as rare as possible, that means with regions as big as possible.
    // Logic is based on the fact that all errors for each particular file are stored continuously in the myItems model

    boolean selectedProblemRemoved = false;

    int matchedFilesCount = 0;

    for (int i = getRowCount() - 1; i >= 0; i--) {
      final DartProblem problem = getItem(i);
      if (filePaths.contains(problem.getSystemIndependentPath())) {
        matchedFilesCount++;
        final int lastRowToDelete = i;

        if (problem == selectedProblem) {
          selectedProblemRemoved = true;
        }

        DartProblem lastProblemForCurrentFile = problem;

        int j = i - 1;
        while (j >= 0) {
          final DartProblem previousProblem = getItem(j);

          if (previousProblem.getSystemIndependentPath().equals(lastProblemForCurrentFile.getSystemIndependentPath())) {
            // previousProblem should be removed from the table as well
            j--;

            if (previousProblem == selectedProblem) {
              selectedProblemRemoved = true;
            }

            continue;
          }

          if (filePaths.contains(previousProblem.getSystemIndependentPath())) {
            matchedFilesCount++;
            // continue iterating the table because we met a range of problems for another file that also should be removed
            lastProblemForCurrentFile = previousProblem;
            j--;

            if (previousProblem == selectedProblem) {
              selectedProblemRemoved = true;
            }

            continue;
          }

          break;
        }

        final int firstRowToDelete = j + 1;
        removeRows(firstRowToDelete, lastRowToDelete);

        if (matchedFilesCount == filePaths.size()) {
          break;
        }

        //noinspection AssignmentToForLoopParameter
        i = j + 1; // rewind according to the amount of removed rows
      }
    }

    return selectedProblemRemoved;
  }

  @Nullable
  private DartProblem addErrorsAndReturnReplacementForSelection(@NotNull final Map<String, List<AnalysisError>> filePathToErrors,
                                                                @Nullable final DartProblem oldSelectedProblem) {
    DartProblem newSelectedProblem = null;
    final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode = myPresentationHelper.getScopedAnalysisMode();

    final List<DartProblem> problemsToAdd = new ArrayList<>();
    for (Map.Entry<String, List<AnalysisError>> entry : filePathToErrors.entrySet()) {
      final String filePath = entry.getKey();
      final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
      final boolean fileOk = vFile != null && (scopedAnalysisMode != DartProblemsViewSettings.ScopedAnalysisMode.All ||
                                               ProjectFileIndex.getInstance(myProject).isInContent(vFile));
      final List<AnalysisError> errors = fileOk ? entry.getValue() : AnalysisError.EMPTY_LIST;

      for (AnalysisError analysisError : errors) {
        if (DartAnnotator.shouldIgnoreMessageFromDartAnalyzer(filePath, analysisError.getLocation().getFile())) {
          continue;
        }

        final DartProblem problem = new DartProblem(myProject, analysisError);
        problemsToAdd.add(problem);

        if (oldSelectedProblem != null &&
            lookSimilar(problem, oldSelectedProblem) &&
            (newSelectedProblem == null ||
             // check if current problem is closer to oldSelectedProblem
             (Math.abs(oldSelectedProblem.getLineNumber() - newSelectedProblem.getLineNumber()) >=
              Math.abs(oldSelectedProblem.getLineNumber() - problem.getLineNumber())))) {
          newSelectedProblem = problem;
        }

        if (AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) myErrorCount++;
        if (AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) myWarningCount++;
        if (AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) myHintCount++;
        updateProblemsCountAfterFilter(problem, true);
      }
    }

    if (!problemsToAdd.isEmpty()) {
      addRows(problemsToAdd);
    }

    return newSelectedProblem;
  }

  private static boolean lookSimilar(@NotNull final DartProblem problem1, @NotNull final DartProblem problem2) {
    return problem1.getSeverity().equals(problem2.getSeverity()) &&
           problem1.getErrorMessage().equals(problem2.getErrorMessage()) &&
           problem1.getSystemIndependentPath().equals(problem2.getSystemIndependentPath());
  }

  private void updateProblemsCountAfterFilter(@NotNull final DartProblem problem, final boolean incrementNotDecrement) {
    if (myPresentationHelper.shouldShowProblem(problem)) {
      if (incrementNotDecrement) {
        if (AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) myErrorCountAfterFilter++;
        if (AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) myWarningCountAfterFilter++;
        if (AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) myHintCountAfterFilter++;
      }
      else {
        if (AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) myErrorCountAfterFilter--;
        if (AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) myWarningCountAfterFilter--;
        if (AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) myHintCountAfterFilter--;
      }
    }
  }

  public void setSortKey(@NotNull final RowSorter.SortKey sortKey) {
    mySortKey = sortKey;
  }

  public void onFilterChanged() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (myPresentationHelper.areFiltersApplied()) {
      myErrorCountAfterFilter = 0;
      myWarningCountAfterFilter = 0;
      myHintCountAfterFilter = 0;
      for (DartProblem problem : myItems) {
        updateProblemsCountAfterFilter(problem, true);
      }
    }
    else {
      myErrorCountAfterFilter = myErrorCount;
      myWarningCountAfterFilter = myWarningCount;
      myHintCountAfterFilter = myHintCount;
    }
  }

  boolean hasErrors() {
    return myErrorCount > 0;
  }

  boolean hasWarnings() {
    return myWarningCount > 0;
  }

  @NotNull
  public String getStatusText() {
    final StringBuilder b = new StringBuilder();
    final List<String> summary = new ArrayList<>();

    if (myPresentationHelper.isShowErrors() && myErrorCountAfterFilter > 0) {
      summary.add(myErrorCountAfterFilter + " " + StringUtil.pluralize("error", myErrorCountAfterFilter));
    }
    if (myPresentationHelper.isShowWarnings() && myWarningCountAfterFilter > 0) {
      summary.add(myWarningCountAfterFilter + " " + StringUtil.pluralize("warning", myWarningCountAfterFilter));
    }
    if (myPresentationHelper.isShowHints() && myHintCountAfterFilter > 0) {
      summary.add(myHintCountAfterFilter + " " + StringUtil.pluralize("hint", myHintCountAfterFilter));
    }


    if (summary.isEmpty()) {
      if (myPresentationHelper.areFiltersApplied()) {
        return getFilterTypeText();
      }
      else {
        return "";
      }
    }

    if (summary.size() == 2) {
      b.append(StringUtil.join(summary, " and "));
    }
    else {
      b.append(StringUtil.join(summary, ", "));
    }

    if (myPresentationHelper.areFiltersApplied()) {
      b.append(" (");
      b.append(getFilterTypeText());
      b.append(")");
    }

    return b.toString();
  }

  private String getFilterTypeText() {
    final StringBuilder builder = new StringBuilder();

    switch (myPresentationHelper.getFileFilterMode()) {
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

    if (!myPresentationHelper.isShowErrors() || !myPresentationHelper.isShowWarnings() || !myPresentationHelper.isShowHints()) {
      builder.append(builder.length() == 0 ? "filtering by severity" : " and severity");
    }

    return builder.toString();
  }

  private class DartProblemsComparator implements Comparator<DartProblem> {
    private static final int MESSAGE_COLUMN_ID = 0;
    private static final int LOCATION_COLUMN_ID = 1;

    private final int myColumn;

    DartProblemsComparator(final int column) {
      myColumn = column;
    }

    @Override
    public int compare(@NotNull final DartProblem problem1, @NotNull final DartProblem problem2) {
      if (myPresentationHelper.isGroupBySeverity()) {
        final int s1 = getSeverityIndex(problem1);
        final int s2 = getSeverityIndex(problem2);
        if (s1 != s2) {
          // Regardless of sorting direction, if 'Group by severity' is selected then we should keep errors on top
          return mySortKey.getSortOrder() == SortOrder.ASCENDING ? s1 - s2 : s2 - s1;
        }
      }

      if (myColumn == MESSAGE_COLUMN_ID) {
        return StringUtil.compare(problem1.getErrorMessage(), problem2.getErrorMessage(), false);
      }

      if (myColumn == LOCATION_COLUMN_ID) {
        final int result = StringUtil.compare(problem1.getPresentableLocationWithoutLineNumber(),
                                              problem2.getPresentableLocationWithoutLineNumber(), false);
        if (result != 0) {
          return result;
        }
        else {
          // Regardless of sorting direction, line numbers within the same file should be sorted in ascending order
          return mySortKey.getSortOrder() == SortOrder.ASCENDING
                 ? problem1.getLineNumber() - problem2.getLineNumber()
                 : problem2.getLineNumber() - problem1.getLineNumber();
        }
      }

      return 0;
    }

    private int getSeverityIndex(@NotNull final DartProblem problem) {
      final String severity = problem.getSeverity();
      if (AnalysisErrorSeverity.ERROR.equals(severity)) {
        return 0;
      }
      if (AnalysisErrorSeverity.WARNING.equals(severity)) {
        return 1;
      }
      return 2;
    }
  }
}
