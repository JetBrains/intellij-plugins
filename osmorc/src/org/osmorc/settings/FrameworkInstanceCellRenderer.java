package org.osmorc.settings;

import com.intellij.ide.ui.ListCellRendererWrapper;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public abstract class FrameworkInstanceCellRenderer extends ListCellRendererWrapper {
  public FrameworkInstanceCellRenderer(ListCellRenderer renderer) {
    super(renderer);
  }

  @Override
  public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
    final String str = value != null ? value.toString() : null;
    if (str != null) {
      if (!isInstanceDefined(((FrameworkInstanceDefinition)value))) {
        setText(str + " [invalid]");
        setForeground(Color.RED);
      }
      else {
        setText(str);
      }
    }
  }

  protected abstract boolean isInstanceDefined(FrameworkInstanceDefinition instance);
}
