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
package jetbrains.communicator.idea.toolWindow;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author Kir
 */
public class TreeDragListener extends MouseAdapter implements MouseMotionListener {
  private MouseEvent myDragStartedEvent;

  public void mousePressed(MouseEvent e) {
    myDragStartedEvent = e;
  }

  public void mouseReleased(MouseEvent e) {
    cancelDrag();
  }

  public void mouseDragged(MouseEvent e) {
    if (myDragStartedEvent != null) {
      if (e.getComponent() instanceof JTree) {
        JTree tree = ((JTree) e.getComponent());

        int startX = myDragStartedEvent.getX();
        int startY = myDragStartedEvent.getY();
        TreePath startDragLocation = tree.getPathForLocation(startX, startY);
        if (!tree.isPathSelected(startDragLocation)) {
          cancelDrag();
          return;
        }

        if (e.getY() - startY < 5) {
          tree.getTransferHandler().
            exportAsDrag((JComponent) e.getComponent(), myDragStartedEvent, TransferHandler.MOVE);
          e.consume();
        }
      }
    }
    cancelDrag();
  }

  private void cancelDrag() {
    myDragStartedEvent = null;
  }

  public void mouseMoved(MouseEvent e) {
  }
}
