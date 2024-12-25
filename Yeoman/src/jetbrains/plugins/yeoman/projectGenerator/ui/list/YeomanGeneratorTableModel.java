package jetbrains.plugins.yeoman.projectGenerator.ui.list;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.SortableColumnModel;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YeomanGeneratorTableModel extends AbstractTableModel implements SortableColumnModel {

  private final ColumnInfo[] myColumnInfos;

  private final List<YeomanGeneratorInfo> myAllView = new ArrayList<>();
  private final List<YeomanGeneratorInfo> myView = new ArrayList<>();

  public YeomanGeneratorTableModel() {
    myColumnInfos = new ColumnInfo[] {new ColumnInfo<YeomanGeneratorInfo, String>(YeomanBundle.message("column.info.generators")) {
      @Override
      public @Nullable String valueOf(YeomanGeneratorInfo o) {
        return o.getYoName();
      }

      @Override
      public @Nullable TableCellRenderer getRenderer(YeomanGeneratorInfo info) {
        return new YeomanGeneratorCellRenderer(info);
      }
    }};
  }

  @Override
  public ColumnInfo[] getColumnInfos() {
    return myColumnInfos;
  }

  @Override
  public void setSortable(boolean aBoolean) {

  }

  @Override
  public boolean isSortable() {
    return true;
  }

  @Override
  public Object getRowValue(int row) {
    return myView.get(row);
  }

  @Override
  public @Nullable RowSorter.SortKey getDefaultSortKey() {
    return null;
  }

  @Override
  public int getRowCount() {
    return myView.size();
  }

  @Override
  public int getColumnCount() {
    return myColumnInfos.length;
  }

  public YeomanGeneratorInfo getObjectAt (int row) {
    return myView.get(row);
  }

  public void setAllViews(Collection<? extends YeomanGeneratorInfo> infos) {
    myAllView.clear();
    myAllView.addAll(infos);

    filter(null);
  }

  protected void filter(@Nullable String filter) {
    myView.clear();
    if (StringUtil.isEmpty(filter)) {
      myView.addAll(myAllView);

      fireTableDataChanged();
      return;
    }

    for (YeomanGeneratorInfo info : myAllView) {
      if (StringUtil.containsIgnoreCase(info.getYoName(), filter)) {
        myView.add(info);
      }
    }

    fireTableDataChanged();
  }


  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    //noinspection unchecked
    return myColumnInfos[columnIndex].valueOf(getRowValue(rowIndex));
  }

  public List<YeomanGeneratorInfo> getView() {
    return myView;
  }
}
