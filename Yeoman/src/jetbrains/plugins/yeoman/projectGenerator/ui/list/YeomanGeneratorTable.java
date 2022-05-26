package jetbrains.plugins.yeoman.projectGenerator.ui.list;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ColumnInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class YeomanGeneratorTable extends JBTable {

  public YeomanGeneratorTable(final YeomanGeneratorTableModel model) {
    super(model);
    getColumnModel().setColumnMargin(0);

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setShowGrid(false);
    this.setTableHeader(null);
  }

  @Override
  public TableCellRenderer getCellRenderer(final int row, final int column) {
    final YeomanGeneratorTableModel model = (YeomanGeneratorTableModel)getModel();
    final ColumnInfo columnInfo = model.getColumnInfos()[column];
    //noinspection unchecked
    return columnInfo.getRenderer(model.getRowValue(row));
  }

  public YeomanGeneratorInfo[] getSelectedObjects() {
    YeomanGeneratorInfo[] selection = null;
    if (getSelectedRowCount() > 0) {
      int[] poses = getSelectedRows();
      selection = new YeomanGeneratorInfo[poses.length];
      for (int i = 0; i < poses.length; i++) {
        selection[i] = getObjectAt(poses[i]);
      }
    }
    return selection;
  }

  public YeomanGeneratorInfo getObjectAt(int row) {
    return ((YeomanGeneratorTableModel)getModel()).getObjectAt(convertRowIndexToModel(row));
  }

  public void select(YeomanGeneratorInfo... descriptors) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    YeomanGeneratorTableModel tableModel = (YeomanGeneratorTableModel)getModel();
    getSelectionModel().clearSelection();
    if (descriptors == null) return;

    for (int i = 0; i < tableModel.getRowCount(); i++) {
      YeomanGeneratorInfo descriptorAt = getObjectAt(i);
      if (ArrayUtil.find(descriptors, descriptorAt) != -1) {
        final int row = convertRowIndexToView(i);
        getSelectionModel().addSelectionInterval(row, row);
      }
    }
    TableUtil.scrollSelectionToVisible(this);
  }

  @Override
  protected boolean isSortOnUpdates() {
    return false;
  }

  public Object[] getElements() {
    return ((YeomanGeneratorTableModel)getModel()).getView().toArray();
  }
}
