// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.StartupUiUtil;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.TreeUtils;
import jetbrains.communicator.util.UserActionWithValue;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public class UserTreeRenderer extends ColoredTreeCellRenderer {
  private final JTree myTree;

  public UserTreeRenderer(JTree tree) {
    myTree = tree;
    setFont(StartupUiUtil.getLabelFont());
  }

  @Override
  public void updateUI() {
    super.updateUI();
    setFont(StartupUiUtil.getLabelFont());
  }

  @Override
  protected void paintComponent(final Graphics g) {
    if (getParent() != null && getParent().getParent() == null) {
      myTree.add(getParent());
      getParent().setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    super.paintComponent(g);
  }

  @Override
  public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

    TreeUtils.convertValueIfUserNode(value, new UserActionWithValue() {
      @Override
      public Object execute(User user) {
        append(user.getDisplayName() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        append(user.getPresence().getDisplayText(), SimpleTextAttributes.GRAYED_ATTRIBUTES);

        setIcon(user.getIcon());

        customizeUserNode(user);

        return null;
      }
    });


    if (TreeUtils.getUserObject(value) instanceof String) { // group
      setIcon(IdeTalkCoreIcons.Nodes.Group_close);

      String group = value.toString();
      append(group, getGroupAttributes(group));
    }

  }

  protected void customizeUserNode(User user){}

  protected SimpleTextAttributes getGroupAttributes(String group) {
    return SimpleTextAttributes.REGULAR_ATTRIBUTES;
  }
}
