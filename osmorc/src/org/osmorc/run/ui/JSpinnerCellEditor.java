package org.osmorc.run.ui;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.EventObject;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class JSpinnerCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private final JSpinner mySpinner = new JSpinner();

  public JSpinnerCellEditor() {
    mySpinner.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)mySpinner.getEditor();
    editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(new MyNumberFormatter("Default")));
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value == null || !(value instanceof Number)) {
      Logger.getInstance(getClass()).error("value:" + value);
      value = 0;
    }
    mySpinner.setValue(value);
    adjust(table, column);
    return mySpinner;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    mySpinner.setValue(value);
    mySpinner.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        fireEditingStopped();
      }
    });
    mySpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fireEditingStopped();
      }
    });
    adjust(table, column);
    return mySpinner;
  }

  private void adjust(JTable table, int column) {
    Dimension size = mySpinner.getPreferredSize();
    size.width = table.getColumnModel().getColumn(column).getWidth() - 5;
    mySpinner.setPreferredSize(size);
  }

  @Override
  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent)evt).getClickCount() >= 1;
  }

  @Override
  public Object getCellEditorValue() {
    return mySpinner.getValue();
  }

  @Override
  public boolean stopCellEditing() {
    try {
      mySpinner.commitEdit();
      return super.stopCellEditing();
    }
    catch (ParseException e) {
      return false;
    }
  }


  public static class MyNumberFormatter extends NumberFormatter {
    private final String myZeroValue;

    public MyNumberFormatter(@NotNull String zeroValue) {
      myZeroValue = zeroValue;
      setValueClass(Integer.class);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
      if ((value instanceof Long && value.equals(0L)) || (value instanceof Integer && value.equals(0))) {
        return myZeroValue;
      }
      else {
        return super.valueToString(value);
      }
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
      if (text.equals(myZeroValue)) {
        return 0;
      }
      else {
        return super.stringToValue(text);
      }
    }
  }
}
