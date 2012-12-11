package org.osmorc.settings;

import com.intellij.ui.JBColor;
import com.intellij.ui.ListCellRendererWrapper;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public abstract class FrameworkInstanceCellRenderer extends ListCellRendererWrapper {
  public FrameworkInstanceCellRenderer(ListCellRenderer renderer) {
    super();
  }

  @Override
  public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
    final String str = value != null ? value.toString() : null;
    if (str != null) {
      if (!isInstanceDefined(((FrameworkInstanceDefinition)value))) {
        setText(str + " [invalid]");
        setForeground(JBColor.RED);
      }
      else {
        setText(str);
      }
    }
  }

  protected abstract boolean isInstanceDefined(FrameworkInstanceDefinition instance);
}
