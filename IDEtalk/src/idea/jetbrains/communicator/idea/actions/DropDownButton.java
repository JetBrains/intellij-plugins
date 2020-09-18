// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.util.ui.JBUI;
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

      icon.addIcon(AllIcons.General.ArrowDown);
      setModel(new MyButtonModel());
    }

    setIcon(icon);
    setMargin(JBUI.emptyInsets());

    setHorizontalAlignment(JButton.LEFT);
    setFocusable(false);

    addActionListener(
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (hasSeveralActions()) {
            showPopup();
          }
          else {
            AnAction[] children = myActionGroup.getChildren(null);
            if (children.length > 0 && canExecute(children[0])) {
              children[0].actionPerformed(AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataId -> null));
            }
          }
        }
      }
    );
  }

  private boolean canExecute(AnAction action1) {
    Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(this));
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
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          myForcePressed = false;
          repaint();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
      }
    );
    repaint();

    myForcePressed = true;
    JBPopupMenu.showBelow(this, menuComponent);
  }

  public static JComponent wrap(final DropDownButton optionsButton) {
    JPanel panel = new JPanel() {
      @Override
      public Dimension getPreferredSize() {
        Dimension preferredSize = optionsButton.getPreferredSize();
        preferredSize.height += 2;
        return preferredSize;
      }

      @Override
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }

      @Override
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

    @Override
    public boolean isPressed() {
      return myForcePressed || super.isPressed();
    }

    @Override
    public boolean isArmed() {
      return myForcePressed || super.isArmed();
    }
  }
}
