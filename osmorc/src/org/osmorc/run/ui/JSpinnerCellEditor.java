/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osmorc.run.ui;

import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.EventObject;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thomä</a>
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
    mySpinner.setValue(ObjectUtils.notNull(value, 0));
    adjust(table, column);
    return mySpinner;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    mySpinner.setValue(value);
    mySpinner.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        fireEditingStopped();
      }
    });
    mySpinner.addChangeListener(new ChangeListener() {
      @Override
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
