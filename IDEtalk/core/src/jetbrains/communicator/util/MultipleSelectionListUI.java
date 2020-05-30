// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import com.intellij.openapi.wm.IdeFocusManager;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/** It contains a copy of MouseInputHandler from BasicListUI with one
 * correction - multiple selection by mouse is done without Ctrl/Shift */
public class MultipleSelectionListUI extends BasicListUI {
  private int myLastDraggedRow = -1;
  private int myLastPressedRow = -1;

  @Override
  protected MouseInputListener createMouseInputListener() {
    return new MouseInputListener() {
      @Override
      public void mouseClicked(MouseEvent e) {}
      @Override
      public void mouseEntered(MouseEvent e) {}
      @Override
      public void mouseExited(MouseEvent e) {}
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isConsumed()) {
          selectedOnPress = false;
          return;
        }
        selectedOnPress = true;
        adjustFocusAndSelection(e);
      }

      void adjustFocusAndSelection(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
          return;
        }

        if (!list.isEnabled()) {
          return;
        }

        /* Request focus before updating the list selection.  This implies
         * that the current focus owner will see a focusLost() event
         * before the lists selection is updated IF requestFocus() is
         * synchronous (it is on Windows).  See bug 4122345
         */
        if (!list.hasFocus() && list.isRequestFocusEnabled()) {
          IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(list, true));
        }

        int row = locationToIndex(list, e.getPoint());
        if (row != -1) {
          myLastPressedRow = row;
          boolean adjusting = e.getID() == MouseEvent.MOUSE_PRESSED;
          list.setValueIsAdjusting(adjusting);
          int anchorIndex = list.getAnchorSelectionIndex();
          if (e.isShiftDown() && (anchorIndex != -1)) {
            list.setSelectionInterval(anchorIndex, row);
          } else {
            toggleSelection(row);
          }
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (e.isConsumed()) {
          return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
          return;
        }
        if (!list.isEnabled()) {
          return;
        }
        if (e.isShiftDown() || e.isControlDown()) {
          return;
        }

        int row = locationToIndex(list, e.getPoint());
        if (row != -1 && row != myLastDraggedRow && row != myLastPressedRow) {
          myLastDraggedRow = row;
          Rectangle cellBounds = getCellBounds(list, row, row);
          if (cellBounds != null) {
            list.scrollRectToVisible(cellBounds);
            toggleSelection(row);
            //list.setSelectionInterval(row, row);
          }
        }
        if (row == -1) {
          list.clearSelection();
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (selectedOnPress) {
          if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
          }

          list.setValueIsAdjusting(false);
        } else {
          adjustFocusAndSelection(e);
        }
      }

      private boolean selectedOnPress;

    };
  }

  private void toggleSelection(int row) {
    if (list.isSelectedIndex(row)) {
      list.removeSelectionInterval(row, row);
    } else {
      list.addSelectionInterval(row, row);
    }
  }
}
