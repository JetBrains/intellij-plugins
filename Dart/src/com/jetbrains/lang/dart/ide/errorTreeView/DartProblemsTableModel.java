package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.jetbrains.lang.dart.ide.annotator.DartAnnotator;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

class DartProblemsTableModel extends ListTableModel<DartProblem> {

  private static final TableCellRenderer MESSAGE_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // Do not emphasize focused cell, drawing the whole row as selected is enough
      final JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

      final DartProblem problem = (DartProblem)value;
      setText(problem.getErrorMessage());

      final String severity = problem.getSeverity();
      setIcon(AnalysisErrorSeverity.ERROR.equals(severity)
              ? AllIcons.General.Error
              : AnalysisErrorSeverity.WARNING.equals(severity)
                ? DartIcons.Dart_warning
                : AllIcons.General.Information);

      return label;
    }
  };

  private static final TableCellRenderer LOCATION_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // Do not emphasize focused cell, drawing the whole row as selected is enough
      return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
  };

  private final Project myProject;
  @NotNull private final DartProblemsFilter myFilter;

  // Kind of hack to keep a reference to the live collection used in a super class, but it allows to improve performance greatly.
  // Having it in hands we can do bulk rows removal with a single fireTableRowsDeleted() call afterwards
  private final List<DartProblem> myItems;

  private boolean myGroupBySeverity = true;
  private RowSorter.SortKey mySortKey = new RowSorter.SortKey(1, SortOrder.ASCENDING);

  private int myErrorCount = 0;
  private int myWarningCount = 0;
  private int myHintCount = 0;

  private int myErrorCountAfterFilter = 0;
  private int myWarningCountAfterFilter = 0;
  private int myHintCountAfterFilter = 0;

  private final Comparator<DartProblem> myDescriptionComparator = new DartProblemsComparator(DartProblemsComparator.MESSAGE_COLUMN_ID);
  private final Comparator<DartProblem> myLocationComparator = new DartProblemsComparator(DartProblemsComparator.LOCATION_COLUMN_ID);

  public DartProblemsTableModel(@NotNull final Project project, @NotNull final DartProblemsFilter filter) {
    myProject = project;
    myFilter = filter;
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
   * If <code>selectedProblem</code> was removed and similar one added again then this method returns the added one,
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

    final List<DartProblem> problemsToAdd = new ArrayList<>();
    for (Map.Entry<String, List<AnalysisError>> entry : filePathToErrors.entrySet()) {
      final String filePath = entry.getKey();
      final List<AnalysisError> errors = entry.getValue();

      for (AnalysisError analysisError : errors) {
        if (DartAnnotator.shouldIgnoreMessageFromDartAnalyzer(filePath, analysisError.getType(), analysisError.getLocation().getFile())) {
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
    if (myFilter.include(problem)) {
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

  public boolean isGroupBySeverity() {
    return myGroupBySeverity;
  }

  public void setGroupBySeverity(boolean groupBySeverity) {
    myGroupBySeverity = groupBySeverity;
  }

  public void setSortKey(@NotNull final RowSorter.SortKey sortKey) {
    mySortKey = sortKey;
  }

  public void onFilterChanged() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (myFilter.areFiltersApplied()) {
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

  @NotNull
  public String getStatusText() {
    final StringBuilder b = new StringBuilder();
    b.append("Total: ");
    b.append(myErrorCount == 0 ? "no errors, " : myErrorCount == 1 ? "1 error, " : myErrorCount + " errors, ");
    b.append(myWarningCount == 0 ? "no warnings, " : myWarningCount == 1 ? "1 warning, " : myWarningCount + " warnings, ");
    b.append(myHintCount == 0 ? "no hints. " : myHintCount == 1 ? "1 hint. " : myHintCount + " hints. ");

    if (myFilter.areFiltersApplied()) {
      switch (myFilter.getFileFilterMode()) {
        case All:
          if (!myFilter.isShowErrors() || !myFilter.isShowWarnings() || !myFilter.isShowHints()) {
            // should be always true
            b.append("Filtered by severity: ").append(getHiddenSeveritiesText()).append(".");
          }
          break;
        case ContentRoot:
          b.append("Filtered by current content root");
          break;
        case Package:
          b.append("Filtered by current package");
          break;
        case Directory:
          b.append("Filtered by current directory");
          break;
        case File:
          b.append("Filtered by current file");
          break;
      }

      if (myFilter.getFileFilterMode() != DartProblemsFilter.FileFilterMode.All) {
        if (!myFilter.isShowErrors() || !myFilter.isShowWarnings() || !myFilter.isShowHints()) {
          b.append(" and severity: ");

          if (!myFilter.isShowErrors() && !myFilter.isShowWarnings() && !myFilter.isShowHints()) {
            b.append(getHiddenSeveritiesText()).append("."); // everything is filtered out
          }
          else {
            b.append(getProblemsCountAfterFilterText()).append(", ").append(getHiddenSeveritiesText()).append(".");
          }
        }
        else {
          b.append(": ").append(getProblemsCountAfterFilterText()).append(".");
        }
      }
    }

    return b.toString();
  }

  private String getHiddenSeveritiesText() {
    final StringBuilder b = new StringBuilder();
    if (!myFilter.isShowErrors()) b.append("errors");
    if (!myFilter.isShowWarnings()) b.append(b.length() == 0 ? "warnings" : " and warnings");
    if (!myFilter.isShowHints()) b.append(b.length() == 0 ? "hints" : " and hints");
    b.append(" hidden");
    return b.toString();
  }

  private String getProblemsCountAfterFilterText() {
    final StringBuilder b = new StringBuilder();
    if (myFilter.isShowErrors()) {
      b.append(myErrorCountAfterFilter == 0 ? "no errors" : myErrorCountAfterFilter == 1 ? "1 error" : myErrorCountAfterFilter + " errors");
    }
    if (myFilter.isShowWarnings()) {
      if (b.length() > 0) b.append(", ");
      b.append(myWarningCountAfterFilter == 0
               ? "no warnings"
               : myWarningCountAfterFilter == 1 ? "1 warning" : myWarningCountAfterFilter + " warnings");
    }
    if (myFilter.isShowHints()) {
      if (b.length() > 0) b.append(", ");
      b.append(myHintCountAfterFilter == 0 ? "no hints" : myHintCountAfterFilter == 1 ? "1 hint" : myHintCountAfterFilter + " hints");
    }

    return b.toString();
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
      if (myGroupBySeverity) {
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
