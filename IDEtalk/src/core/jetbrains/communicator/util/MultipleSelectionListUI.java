/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package jetbrains.communicator.util;

import com.intellij.ui.components.JBList;

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

  protected MouseInputListener createMouseInputListener() {
    return new MouseInputListener() {
      public void mouseClicked(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {}
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
          list.requestFocus();
        }

        int row = locationToIndex(list, e.getPoint());
        if (row != -1) {
          myLastPressedRow = row;
          boolean adjusting = (e.getID() == MouseEvent.MOUSE_PRESSED) ? true : false;
          list.setValueIsAdjusting(adjusting);
          int anchorIndex = list.getAnchorSelectionIndex();
          if (e.isShiftDown() && (anchorIndex != -1)) {
            list.setSelectionInterval(anchorIndex, row);
          } else {
            toggleSelection(row);
          }
        }
      }

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

      public void mouseMoved(MouseEvent e) {
      }

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

  public static void main(String[] args) {
    final JList jList = new JBList(new Object[] {"ffff", "ffffff","ffff", "ffffff","ffff", "ffffff", "sdfsdfsdf"});
    jList.setUI(new MultipleSelectionListUI());

    UIUtil.run(jList);

  }

}
