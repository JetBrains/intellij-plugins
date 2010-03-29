package org.osmorc.settings;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public abstract class FrameworkInstanceCellRenderer extends ColoredListCellRenderer {
  @Override
  protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
    final String str = value.toString();
    if (str != null) {
      if (!isInstanceDefined(((FrameworkInstanceDefinition)value))) {
        append(str + " [invalid]", SimpleTextAttributes.ERROR_ATTRIBUTES);
      }
      else {
        append(str, selected ? SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES : SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
      }
    }
  }

  protected abstract boolean isInstanceDefined(FrameworkInstanceDefinition instance);
}
