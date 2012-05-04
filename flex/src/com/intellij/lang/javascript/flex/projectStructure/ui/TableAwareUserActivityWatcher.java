package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ui.UserActivityWatcher;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This <code>UserActivityWatcher</code> doesn't listen to changes for child components of any <code>JTable</code>
 * (thus doesn't fire events when cell editing is in progress),
 * but instead fires event when any table cell editing is stopped.
 */
public class TableAwareUserActivityWatcher extends UserActivityWatcher {

  protected void processComponent(final Component component) {
    if (component instanceof JTable) {
      component.addPropertyChangeListener("tableCellEditor", new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent e) {
          if (e.getOldValue() != null && e.getNewValue() == null) {
            // cell editing stopped
            fireUIChanged();
          }
        }
      });
    }
    else {
      Component parent = component;
      while ((parent = parent.getParent()) != null) {
        if (parent instanceof JTable) {
          return;
        }
      }
    }

    super.processComponent(component);
  }
}
