package com.intellij.lang.javascript.flex.build;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class NoBackgroundBooleanTableCellRenderer extends JCheckBox implements TableCellRenderer {
  private final JPanel myPanel = new JPanel();

  public NoBackgroundBooleanTableCellRenderer() {
    super();
    setHorizontalAlignment(JLabel.CENTER);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value == null) {
      myPanel.setBackground(table.getBackground());
      return myPanel;
    }
    else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
      setSelected(((Boolean)value).booleanValue());
      return this;
    }
  }
}
