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
package jetbrains.communicator.idea;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.TreeUtils;
import jetbrains.communicator.util.UIUtil;
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
    setFont(UIManager.getFont("Label.font"));
  }

  public void updateUI() {
    super.updateUI();
    setFont(UIManager.getFont("Label.font"));
  }

  protected void paintComponent(final Graphics g) {
    if (getParent() != null && getParent().getParent() == null) {
      myTree.add(getParent());
      getParent().setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    super.paintComponent(g);
  }

  public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

    TreeUtils.convertValueIfUserNode(value, new UserActionWithValue() {
      public Object execute(User user) {
        append(user.getDisplayName() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        append(user.getPresence().getDisplayText(), SimpleTextAttributes.GRAYED_ATTRIBUTES);

        setIcon(UIUtil.getUserIcon(user));

        customizeUserNode(user);

        return null;
      }
    });


    if (TreeUtils.getUserObject(value) instanceof String) { // group
      if (leaf) {
        setIcon(IconLoader.getIcon("/nodes/group_close.png"));
      }
      else {
        setIcon(IconLoader.getIcon(expanded ? "/nodes/group_open.png" : "/nodes/group_close.png"));
      }

      String group = value.toString();
      append(group, getGroupAttributes(group));
    }

  }

  protected void customizeUserNode(User user){}

  protected SimpleTextAttributes getGroupAttributes(String group) {
    return SimpleTextAttributes.REGULAR_ATTRIBUTES;
  }
}
