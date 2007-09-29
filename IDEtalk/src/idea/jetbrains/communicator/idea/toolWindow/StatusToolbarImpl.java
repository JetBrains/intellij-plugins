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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.ide.StatusToolbar;
import jetbrains.communicator.idea.actions.BaseAction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.Map;

/**
 * @author Kir
 */
public class StatusToolbarImpl implements StatusToolbar {

  private List<Class<? extends NamedUserCommand>> myToolbarCommands = new ArrayList<Class<? extends NamedUserCommand>>();
  private final Map<JPanel, Object> myPanels = new WeakHashMap<JPanel, Object>();

  public void addToolbarCommand(Class<? extends NamedUserCommand> namedCommandClass) {
    myToolbarCommands.add(namedCommandClass);
    for (JPanel panel : myPanels.keySet()) {
      rebuild(panel);
    }
  }

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
    for (Class<? extends NamedUserCommand> toolbarAction : getToolbarActions()) {
      actions.add(new BaseAction(toolbarAction));
    }
    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("BottomToolbar", actions, true);
    actionToolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
    return actionToolbar.getComponent();
  }

  Class<? extends NamedUserCommand>[] getToolbarActions() {
    //noinspection unchecked
    return myToolbarCommands.toArray(new Class[myToolbarCommands.size()]);
  }


}
