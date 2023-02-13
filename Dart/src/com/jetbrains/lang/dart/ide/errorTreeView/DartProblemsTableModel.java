// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.jetbrains.lang.dart.DartBundle;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.Nls;
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
      // Do not emphasize a focused cell, drawing the whole row as selected is enough
      JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

      DartProblem problem = (DartProblem)value;
      setText(problem.getErrorMessage().replaceAll("(\n)+", " "));

      // Pass null to the url, mouse movement to the hover makes the tooltip go away, see https://youtrack.jetbrains.com/issue/WEB-39449
      setToolTipText(DartProblem.generateTooltipText(problem.getErrorMessage(),
                                                     problem.getContextMessages(),
                                                     problem.getCorrectionMessage(),
                                                     null));

      String severity = problem.getSeverity();
      setIcon(AnalysisErrorSeverity.ERROR.equals(severity)
              ? AllIcons.General.Error
              : AnalysisErrorSeverity.WARNING.equals(severity)
                ? AllIcons.General.Warning
                : AllIcons.General.Information);

      return label;
    }
  };

  private static final TableCellRenderer LOCATION_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // Do not emphasize a focused cell, drawing the whole row as selected is enough
      return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
  };

  private final @NotNull Project myProject;
  private final @NotNull DartProblemsPresentationHelper myPresentationHelper;

  // Kind of hack to keep a reference to the live collection used in a super class, but it allows improving performance greatly.
  // Having it in hand we can do bulk rows removal with a single fireTableRowsDeleted() call afterwards
  private final List<DartProblem> myItems;

  private RowSorter.SortKey mySortKey = new RowSorter.SortKey(1, SortOrder.ASCENDING);

  private int myErrorCount;
  private int myWarningCount;
  private int myHintCount;

  private int myErrorCountAfterFilter;
  private int myWarningCountAfterFilter;
  private int myHintCountAfterFilter;

  private final Comparator<DartProblem> myDescriptionComparator = new DartProblemsComparator(DartProblemsComparator.MESSAGE_COLUMN_ID);
  private final Comparator<DartProblem> myLocationComparator = new DartProblemsComparator(DartProblemsComparator.LOCATION_COLUMN_ID);

  DartProblemsTableModel(@NotNull Project project, @NotNull DartProblemsPresentationHelper presentationHelper) {
    this(project, presentationHelper, new ArrayList<>());
  }

  private DartProblemsTableModel(@NotNull Project project,
                                 @NotNull DartProblemsPresentationHelper presentationHelper,
                                 @NotNull ArrayList<DartProblem> items) {
    super(ColumnInfo.EMPTY_ARRAY, items, 0, SortOrder.ASCENDING);
    myProject = project;
    myPresentationHelper = presentationHelper;
    myItems = items;
    setColumnInfos(new ColumnInfo[]{createDescriptionColumn(), createLocationColumn()});
    setSortable(true);
  }

  private @NotNull ColumnInfo<DartProblem, DartProblem> createDescriptionColumn() {
    return new ColumnInfo<>(DartBundle.message("dart.problems.view.column.name.description")) {
      @Override
      public @NotNull Comparator<DartProblem> getComparator() {
        return myDescriptionComparator;
      }

      @Override
      public @NotNull TableCellRenderer getRenderer(@NotNull DartProblem problem) {
        return MESSAGE_RENDERER;
      }

      @Override
      public @NotNull DartProblem valueOf(@NotNull DartProblem problem) {
        return problem;
      }
    };
  }

  private @NotNull ColumnInfo<DartProblem, String> createLocationColumn() {
    return new ColumnInfo<>(DartBundle.message("dart.problems.view.column.name.location")) {
      @Override
      public @NotNull Comparator<DartProblem> getComparator() {
        return myLocationComparator;
      }

      @Override
      public @NotNull TableCellRenderer getRenderer(DartProblem problem) {
        return LOCATION_RENDERER;
      }

      @Override
      public @NotNull String valueOf(@NotNull DartProblem problem) {
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

  private void removeRows(int firstRow, int lastRow) {
    assert lastRow >= firstRow;

    for (int i = lastRow; i >= firstRow; i--) {
      DartProblem removed = myItems.remove(i);

      if (AnalysisErrorSeverity.ERROR.equals(removed.getSeverity())) myErrorCount--;
      if (AnalysisErrorSeverity.WARNING.equals(removed.getSeverity())) myWarningCount--;
      if (AnalysisErrorSeverity.INFO.equals(removed.getSeverity())) myHintCount--;
      updateProblemsCountAfterFilter(removed, -1);
    }

    fireTableRowsDeleted(firstRow, lastRow);
  }

  void removeAll() {
    int rowCount = getRowCount();
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
  DartProblem setProblemsAndReturnReplacementForSelection(@NotNull Map<String, List<? extends AnalysisError>> filePathToErrors,
                                                          @Nullable DartProblem selectedProblem) {
    boolean selectedProblemRemoved = removeRowsForFilesInSet(filePathToErrors.keySet(), selectedProblem);
    return addErrorsAndReturnReplacementForSelection(filePathToErrors, selectedProblemRemoved ? selectedProblem : null);
  }

  private boolean removeRowsForFilesInSet(@NotNull Set<String> filePaths, @Nullable DartProblem selectedProblem) {
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

  private @Nullable DartProblem addErrorsAndReturnReplacementForSelection(@NotNull Map<String, List<? extends AnalysisError>> filePathToErrors,
                                                                          @Nullable DartProblem oldSelectedProblem) {
    DartProblem newSelectedProblem = null;
    DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode = myPresentationHelper.getScopedAnalysisMode();

    List<DartProblem> problemsToAdd = new ArrayList<>();
    for (Map.Entry<String, List<? extends AnalysisError>> entry : filePathToErrors.entrySet()) {
      String filePath = entry.getKey();
      VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
      boolean fileOk = vFile != null && (scopedAnalysisMode != DartProblemsViewSettings.ScopedAnalysisMode.All ||
                                         ProjectFileIndex.getInstance(myProject).isInContent(vFile));
      List<? extends AnalysisError> errors = fileOk ? entry.getValue() : AnalysisError.EMPTY_LIST;

      for (AnalysisError analysisError : errors) {
        DartProblem problem = new DartProblem(myProject, analysisError);
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
        updateProblemsCountAfterFilter(problem, +1);
      }
    }

    if (!problemsToAdd.isEmpty()) {
      addRows(problemsToAdd);
    }

    return newSelectedProblem;
  }

  private static boolean lookSimilar(@NotNull DartProblem problem1, @NotNull DartProblem problem2) {
    return problem1.getSeverity().equals(problem2.getSeverity()) &&
           problem1.getErrorMessage().equals(problem2.getErrorMessage()) &&
           problem1.getSystemIndependentPath().equals(problem2.getSystemIndependentPath());
  }

  private void updateProblemsCountAfterFilter(@NotNull DartProblem problem, int delta) {
    if (myPresentationHelper.shouldShowProblem(problem)) {
      if (AnalysisErrorSeverity.ERROR.equals(problem.getSeverity())) myErrorCountAfterFilter += delta;
      if (AnalysisErrorSeverity.WARNING.equals(problem.getSeverity())) myWarningCountAfterFilter += delta;
      if (AnalysisErrorSeverity.INFO.equals(problem.getSeverity())) myHintCountAfterFilter += delta;
    }
  }

  void setSortKey(@NotNull RowSorter.SortKey sortKey) {
    mySortKey = sortKey;
  }

  void onFilterChanged() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (myPresentationHelper.areFiltersApplied()) {
      myErrorCountAfterFilter = 0;
      myWarningCountAfterFilter = 0;
      myHintCountAfterFilter = 0;
      for (DartProblem problem : myItems) {
        updateProblemsCountAfterFilter(problem, +1);
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

  @NotNull @Nls
  String getTabTitleText() {
    List<String> statusParts = new ArrayList<>();

    if (myPresentationHelper.isShowErrors() && myErrorCountAfterFilter > 0) {
      statusParts.add(DartBundle.message("dart.problems.view.0.errors", myErrorCountAfterFilter));
    }
    if (myPresentationHelper.isShowWarnings() && myWarningCountAfterFilter > 0) {
      statusParts.add(DartBundle.message("dart.problems.view.0.warnings", myWarningCountAfterFilter));
    }
    if (myPresentationHelper.isShowHints() && myHintCountAfterFilter > 0) {
      statusParts.add(DartBundle.message("dart.problems.view.0.hints", myHintCountAfterFilter));
    }

    if (statusParts.isEmpty()) {
      return myPresentationHelper.areFiltersApplied() ? myPresentationHelper.getFilterTypeText() : "";
    }

    @Nls String statusText;
    if (statusParts.size() == 1) {
      statusText = statusParts.get(0);
    }
    else if (statusParts.size() == 2) {
      statusText = DartBundle.message("dart.problems.view.status.0.and.1", statusParts.get(0), statusParts.get(1));
    }
    else {
      statusText = DartBundle.message("dart.problems.view.status.0.and.1.and.2",
                                      statusParts.get(0), statusParts.get(1), statusParts.get(2));
    }

    if (myPresentationHelper.areFiltersApplied()) {
      return DartBundle.message("dart.problems.view.status.0.and.filters.1", statusText, myPresentationHelper.getFilterTypeText());
    }

    return statusText;
  }

  private class DartProblemsComparator implements Comparator<DartProblem> {
    private static final int MESSAGE_COLUMN_ID = 0;
    private static final int LOCATION_COLUMN_ID = 1;

    private final int myColumn;

    DartProblemsComparator(final int column) {
      myColumn = column;
    }

    @Override
    public int compare(@NotNull DartProblem problem1, @NotNull DartProblem problem2) {
      if (myPresentationHelper.isGroupBySeverity()) {
        int s1 = getSeverityIndex(problem1);
        int s2 = getSeverityIndex(problem2);
        if (s1 != s2) {
          // Regardless of sorting direction, if 'Group by severity' is selected then we should keep errors on top
          return mySortKey.getSortOrder() == SortOrder.ASCENDING ? s1 - s2 : s2 - s1;
        }
      }

      if (myColumn == MESSAGE_COLUMN_ID) {
        return StringUtil.compare(problem1.getErrorMessage(), problem2.getErrorMessage(), false);
      }

      if (myColumn == LOCATION_COLUMN_ID) {
        int result = StringUtil.compare(problem1.getPresentableLocationWithoutLineNumber(),
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

    private static int getSeverityIndex(@NotNull DartProblem problem) {
      String severity = problem.getSeverity();
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
