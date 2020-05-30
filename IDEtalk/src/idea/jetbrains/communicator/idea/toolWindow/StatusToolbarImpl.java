// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.toolWindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.ide.StatusToolbar;
import jetbrains.communicator.idea.actions.BaseAction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class StatusToolbarImpl implements StatusToolbar {
  private final List<Class<? extends NamedUserCommand>> myToolbarCommands = new ArrayList<>();
  private final Map<JPanel, Object> myPanels = new WeakHashMap<>();

  @Override
  public void addToolbarCommand(Class<? extends NamedUserCommand> namedCommandClass) {
    myToolbarCommands.add(namedCommandClass);
    for (JPanel panel : myPanels.keySet()) {
      rebuild(panel);
    }
  }

  @Override
  public Component createComponent() {
    JPanel jPanel = new JPanel(new BorderLayout());
    myPanels.put(jPanel, null);
    rebuild(jPanel);
    return jPanel;
  }

  private void rebuild(JPanel panel) {
    panel.setOpaque(false);
    panel.removeAll();
    panel.add(createBottomPanel());
    panel.revalidate();
  }

  private Component createBottomPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();
    for (Class<? extends NamedUserCommand> toolbarAction : myToolbarCommands) {
      actions.add(new BaseAction<>(toolbarAction));
    }
    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("BottomToolbar", actions, true);
    actionToolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
    return actionToolbar.getComponent();
  }
}
