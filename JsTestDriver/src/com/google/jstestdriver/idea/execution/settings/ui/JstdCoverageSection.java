package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageSection extends AbstractRunSettingsSection {

  private final ExcludedTableModel myExcludedTableModel;
  private final JComponent myComponent;

  public JstdCoverageSection(@NotNull final Project project) {
    myExcludedTableModel = new ExcludedTableModel();
    final JBTable table = new JBTable(myExcludedTableModel);
    table.getEmptyText().setText("No files excluded from coverage");
    table.setRowHeight((int)(table.getRowHeight() * 1.2));
    Dimension preferredScrollableViewportSize = new Dimension(
      table.getPreferredScrollableViewportSize().width,
      table.getRowHeight() * 6
    );
    table.setPreferredScrollableViewportSize(preferredScrollableViewportSize);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setMinimumSize(preferredScrollableViewportSize);

    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setResizingAllowed(false);
    tableHeader.setReorderingAllowed(false);
    tableHeader.setPreferredSize(new Dimension(
      tableHeader.getPreferredSize().width,
      (int) (tableHeader.getPreferredSize().height * 1.2)
    ));

    JScrollPane scrollPane = new JBScrollPane(
      table,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    );
    Dimension scrollDimension = new Dimension(
      preferredScrollableViewportSize.width,
      tableHeader.getPreferredSize().height + preferredScrollableViewportSize.height + 4
    );
    scrollPane.setMinimumSize(scrollDimension);

    myComponent = ToolbarDecorator.createDecorator(table)
      .disableUpAction()
      .disableDownAction()
      .setAddAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
          addPath(project, table);
        }
      }).setRemoveAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
          removePaths(table);
        }
      }).createPanel();
  }

  private static void addPath(@NotNull Project project, @NotNull JBTable table) {
    ExcludedTableModel tableModel = (ExcludedTableModel) table.getModel();
    int selectedIndex = table.getSelectedRow() + 1;
    if (selectedIndex < 0) {
      selectedIndex = tableModel.getRowCount();
    }
    int savedSelected = selectedIndex;
    VirtualFile[] chosen = FileChooser.chooseFiles(FileChooserDescriptorFactory.createAllButJarContentsDescriptor(), project, null);
    for (final VirtualFile chosenFile : chosen) {
      String path = FileUtil.toSystemDependentName(chosenFile.getPath());
      if (tableModel.isFileExcluded(path)) {
        continue;
      }
      tableModel.addPath(path, selectedIndex);
      selectedIndex++;
    }
    if (selectedIndex > savedSelected) {
      tableModel.fireTableRowsInserted(savedSelected, selectedIndex - 1);
      table.setRowSelectionInterval(savedSelected, selectedIndex - 1);
    }
  }

  private static void removePaths(@NotNull JBTable table) {
    int[] selected = table.getSelectedRows();
    if (selected == null || selected.length <= 0) {
      return;
    }
    if (table.isEditing()) {
      TableCellEditor editor = table.getCellEditor();
      if (editor != null) {
        editor.stopCellEditing();
      }
    }
    ExcludedTableModel model = (ExcludedTableModel) table.getModel();
    Arrays.sort(selected);
    int removedCount = 0;
    for (int indexToRemove : selected) {
      final int row = indexToRemove - removedCount;
      model.removeRow(row);
      model.fireTableRowsDeleted(row, row);
      removedCount++;
    }
    IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(table, true));
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    return myComponent;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    List<String> excludedFiles = runSettings.getFilesExcludedFromCoverage();
    myExcludedTableModel.setExcludedFiles(excludedFiles);
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setFilesExcludedFromCoverage(myExcludedTableModel.myExcludedFiles);
  }

  private static class ExcludedTableModel extends AbstractTableModel {
    private final List<String> myExcludedFiles = Lists.newArrayList();

    private void setExcludedFiles(List<String> excludedFiles) {
      myExcludedFiles.clear();
      myExcludedFiles.addAll(excludedFiles);
      fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
      return myExcludedFiles.size();
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
      if (columnIndex == 0) {
        return "Excluded file path";
      }
      throw unexpectedColumn(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex != 0) {
        throw unexpectedColumn(columnIndex);
      }
      checkRowIndex(rowIndex);
      myExcludedFiles.set(rowIndex, String.valueOf(aValue));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (columnIndex != 0) {
        throw unexpectedColumn(columnIndex);
      }
      checkRowIndex(rowIndex);
      return myExcludedFiles.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
      checkRowIndex(rowIndex);
      myExcludedFiles.remove(rowIndex);
    }

    private void checkRowIndex(int rowIndex) {
      if (rowIndex < 0 || rowIndex >= myExcludedFiles.size()) {
        throw new IllegalStateException("Requested excluded file is out of bound: (rowIndex: "
                                        + rowIndex + ", size: " + myExcludedFiles.size() + ")");
      }
    }

    public boolean isFileExcluded(@NotNull String path) {
      return myExcludedFiles.contains(path);
    }

    public void addPath(@NotNull String path, int selectedIndex) {
      myExcludedFiles.add(selectedIndex, path);
    }

    private static IllegalStateException unexpectedColumn(int column) {
      return new IllegalStateException("Unexpected column: " + column);
    }
  }
}
