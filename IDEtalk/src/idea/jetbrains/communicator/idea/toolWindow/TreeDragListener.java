/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  public void mousePressed(MouseEvent e) {
    myDragStartedEvent = e;
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    cancelDrag();
  }

  @Override
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
}
