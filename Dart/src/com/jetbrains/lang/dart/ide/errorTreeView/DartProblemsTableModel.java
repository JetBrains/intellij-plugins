package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.jetbrains.lang.dart.analyzer.DartServerErrorsAnnotator;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.io.File;
import java.util.*;

class DartProblemsTableModel extends ListTableModel<AnalysisError> {

  private static final TableCellRenderer MESSAGE_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      final AnalysisError error = (AnalysisError)value;
      setText(error.getMessage());

      final String severity = error.getSeverity();
      setIcon(AnalysisErrorSeverity.ERROR.equals(severity)
              ? AllIcons.General.Error
              : AnalysisErrorSeverity.WARNING.equals(severity)
                ? DartIcons.Dart_warning
                : AllIcons.General.Information);

      return label;
    }
  };

  private static final ColumnInfo DESCRIPTION_COLUMN = new ColumnInfo<AnalysisError, AnalysisError>("Description") {
    final Comparator<AnalysisError> myComparator = new AnalysisErrorComparator(AnalysisErrorComparator.MESSAGE_COLUMN_ID);

    @Nullable
    @Override
    public Comparator<AnalysisError> getComparator() {
      return myComparator;
    }

    @Nullable
    @Override
    public TableCellRenderer getRenderer(final AnalysisError error) {
      return MESSAGE_RENDERER;
    }

    @NotNull
    @Override
    public AnalysisError valueOf(final AnalysisError error) {
      return error;
    }
  };

  public static final ColumnInfo<AnalysisError, String> FILE_COLUMN = new ColumnInfo<AnalysisError, String>("File") {
    final Comparator<AnalysisError> myComparator = new AnalysisErrorComparator(AnalysisErrorComparator.LOCATION_COLUMN_ID);

    @Nullable
    @Override
    public Comparator<AnalysisError> getComparator() {
      return myComparator;
    }

    @NotNull
    @Override
    public String valueOf(final AnalysisError error) {
      final Location location = error.getLocation();
      final String file = location.getFile();
      final String fileName = new File(file).getName();
      return fileName + ":" + location.getStartLine();
    }
  };

  // Kind of hack to keep a reference to the live collection used in a super class, but it allows to improve performance greatly.
  // Having it in hands we can do bulk rows removal with a single fireTableRowsDeleted() call afterwards
  private final List<AnalysisError> myItems;

  private int myErrorCount = 0;
  private int myWarningCount = 0;
  private int myHintCount = 0;

  public DartProblemsTableModel() {
    super(DESCRIPTION_COLUMN, FILE_COLUMN);

    myItems = new ArrayList<AnalysisError>();
    setItems(myItems);
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
      final AnalysisError removed = myItems.remove(i);

      if (AnalysisErrorSeverity.ERROR.equals(removed.getSeverity())) myErrorCount--;
      if (AnalysisErrorSeverity.WARNING.equals(removed.getSeverity())) myWarningCount--;
      if (AnalysisErrorSeverity.INFO.equals(removed.getSeverity())) myHintCount--;
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
  }

  public void setErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    final Set<String> systemDependentFilePaths = ContainerUtil.map2Set(filePathToErrors.keySet(), new Function<String, String>() {
      @Override
      public String fun(String filePath) {
        return FileUtil.toSystemDependentName(filePath);
      }
    });
    removeRowsForFilesInSet(systemDependentFilePaths);
    addErrors(filePathToErrors);
  }

  private void removeRowsForFilesInSet(@NotNull final Set<String> systemDependentFilePaths) {
    // Looks for regions in table items that should be removed and removes them.
    // For performance reasons we try to call removeRows() as rare as possible, that means with regions as big as possible.
    // Logic is based on the fact that all errors for each particular file are stored continuously in the myItems model

    for (int i = getRowCount() - 1; i >= 0; i--) {
      final AnalysisError error = getItem(i);
      if (systemDependentFilePaths.remove(error.getLocation().getFile())) {
        final int lastRowToDelete = i;

        AnalysisError lastErrorForCurrentFile = error;

        int j = i - 1;
        while (j >= 0) {
          final AnalysisError previousError = getItem(j);

          if (previousError.getLocation().getFile().equals(lastErrorForCurrentFile.getLocation().getFile())) {
            // previousError should be removed from the table as well
            j--;
            continue;
          }

          if (systemDependentFilePaths.remove(previousError.getLocation().getFile())) {
            // continue iterating the table because we met a range of errors for another file that also should be removed
            lastErrorForCurrentFile = previousError;
            j--;
            continue;
          }

          break;
        }

        final int firstRowToDelete = j + 1;
        removeRows(firstRowToDelete, lastRowToDelete);

        if (systemDependentFilePaths.isEmpty()) {
          break;
        }

        //noinspection AssignmentToForLoopParameter
        i = j + 1; // rewind according to the amount of removed rows
      }
    }
  }

  private void addErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    final List<AnalysisError> errorsToAdd = new ArrayList<AnalysisError>();
    for (Map.Entry<String, List<AnalysisError>> entry : filePathToErrors.entrySet()) {
      final String filePath = entry.getKey();
      final List<AnalysisError> errors = entry.getValue();

      for (AnalysisError error : errors) {
        if (DartServerErrorsAnnotator.shouldIgnoreMessageFromDartAnalyzer(filePath, error)) continue;

        errorsToAdd.add(error);

        if (AnalysisErrorSeverity.ERROR.equals(error.getSeverity())) myErrorCount++;
        if (AnalysisErrorSeverity.WARNING.equals(error.getSeverity())) myWarningCount++;
        if (AnalysisErrorSeverity.INFO.equals(error.getSeverity())) myHintCount++;
      }
    }

    if (!errorsToAdd.isEmpty()) {
      addRows(errorsToAdd);
    }
  }

  public int getErrorCount() {
    return myErrorCount;
  }

  public int getWarningCount() {
    return myWarningCount;
  }

  public int getHintCount() {
    return myHintCount;
  }

  static class AnalysisErrorComparator implements Comparator<AnalysisError> {
    public static final int MESSAGE_COLUMN_ID = 0;
    public static final int LOCATION_COLUMN_ID = 1;
    final int myColumn;

    AnalysisErrorComparator(int column) {
      myColumn = column;
    }

    @Override
    public int compare(AnalysisError o1, AnalysisError o2) {
      {
        final int s1 = getSeverity(o1);
        final int s2 = getSeverity(o2);
        if (s1 != s2) {
          return s1 - s2;
        }
      }
      if (myColumn == MESSAGE_COLUMN_ID) {
        final String m1 = o1.getMessage();
        final String m2 = o2.getMessage();
        return StringUtil.compare(m1, m2, false);
      }
      if (myColumn == LOCATION_COLUMN_ID) {
        final Location l1 = o1.getLocation();
        final Location l2 = o2.getLocation();
        // file name
        final String n1 = PathUtil.getFileName(l1.getFile());
        final String n2 = PathUtil.getFileName(l2.getFile());
        final int c = StringUtil.compare(n1, n2, false);
        if (c != 0) {
          return c;
        }
        // line
        return l1.getStartLine() - l2.getStartLine();
      }
      return 0;
    }

    private static int getSeverity(AnalysisError error) {
      final String severity = error.getSeverity();
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
