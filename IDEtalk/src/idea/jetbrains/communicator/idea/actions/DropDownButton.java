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
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.util.icons.CompositeIcon;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Kir
 */
public class DropDownButton extends JButton {
  private boolean myForcePressed;
  private final ActionGroup myActionGroup;

  public DropDownButton(ActionGroup actionGroup, Icon  buttonIcon) {
    if (actionGroup == null) {
      actionGroup = new DefaultActionGroup();
    }

    myActionGroup = actionGroup;

    CompositeIcon icon = new CompositeIcon();

    icon.addIcon(buttonIcon);
    if (hasSeveralActions()) {

      icon.addIcon(AllIcons.General.ComboArrow);
      setModel(new MyButtonModel());
    }

    setIcon(icon);
    setMargin(new Insets(0, 0, 0, 0));

    setHorizontalAlignment(JButton.LEFT);
    setFocusable(false);

    addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (hasSeveralActions()) {
            showPopup();
          }
          else {
            AnAction[] children = myActionGroup.getChildren(null);
            if (children.length > 0 && canExecute(children[0])) {
              children[0].actionPerformed(null);
            }
          }
        }
      }
    );
  }

  private boolean canExecute(AnAction action1) {
    Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(this));
    return
        !(action1 instanceof BaseAction) ||
        ((BaseAction) action1).getCommand(BaseAction.getContainer(project)).isEnabled();
  }

  private boolean hasSeveralActions() {
    return myActionGroup.getChildren(null).length > 1;
  }

  public void showPopup() {

    ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu("POPUP", myActionGroup);
    JPopupMenu menuComponent = menu.getComponent();
    menuComponent.addPopupMenuListener(
      new PopupMenuListener() {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          myForcePressed = false;
          repaint();
        }

        public void popupMenuCanceled(PopupMenuEvent e) {}
      }
    );
    repaint();

    myForcePressed = true;
    menuComponent.show(this, 0, getHeight());
  }

  public static JComponent wrap(final DropDownButton optionsButton) {
    JPanel panel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension preferredSize = optionsButton.getPreferredSize();
        preferredSize.height += 2;
        return preferredSize;
      }

      public Dimension getMinimumSize() {
        return getPreferredSize();
      }

      public void doLayout() {
        super.doLayout();
        optionsButton.setLocation(0, 2);
        optionsButton.setSize(getWidth(), getHeight() - 4);
      }

    };
    panel.setLayout(null);
    panel.add(optionsButton);
    return panel;
  }

  protected class MyButtonModel extends DefaultButtonModel {

    public boolean isPressed() {
      return myForcePressed || super.isPressed();
    }

    public boolean isArmed() {
      return myForcePressed || super.isArmed();
    }
  }
}
