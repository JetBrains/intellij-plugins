// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import com.intellij.ui.treeStructure.Tree;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Kir
 */
public class KirTree extends Tree {
  private static final Logger LOG = Logger.getLogger(KirTree.class);

  public KirTree() {
    setOpaque(true);
    setBackground(com.intellij.util.ui.UIUtil.getTreeBackground());

    setRootVisible(false);
    setShowsRootHandles(true);

    ToolTipManager.sharedInstance().registerComponent(this);

    setCellRenderer(new DefaultTreeCellRenderer(){
      @Override
      public Component getTreeCellRendererComponent(JTree tree1, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus1) {
        Component renderer = super.getTreeCellRendererComponent(tree1, value, sel, expanded, leaf, row, hasFocus1);
        if (renderer instanceof JLabel && value instanceof KirTreeNode) {
          JLabel label = (JLabel) renderer;
          renderer = ((KirTreeNode) value).renderIn(label, sel, hasFocus);
        }
        return renderer;
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        final TreePath closestPath = getPathForLocation(e.getX(), e.getY());
        if (e.getClickCount() == 2) {
          TreePath path = getSelectionPath();
          if (path != null && path.equals(closestPath)) {
            onDblClick(path, path.getLastPathComponent(), e);
          }
        }
        else if (e.getClickCount() == 1) {

          if (e.getButton() == MouseEvent.BUTTON1) {
            UIUtil.invokeLater(() -> {
              TreePath path = getSelectionPath();
              if (path != null && path.equals(closestPath)) {
                onClick(path, path.getLastPathComponent(), e);
              }
            });
          }
          else {
            if (!isPathSelected(closestPath)) {
              setSelectionPath(closestPath);
            }
          }
        }
      }
    });

    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
    getActionMap().put("Enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onEnter();
      }
    });
  }

  protected void onEnter() {
  }

  protected void onDblClick(TreePath path, Object pathComponent, MouseEvent e) {
    onEnter();
  }

  protected void onClick(TreePath path, Object pathComponent, MouseEvent e) {
    LOG.debug("Click on " + pathComponent);
  }
}
