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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.SeparatorOrientation;
import com.intellij.util.IconUtil;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.ide.StatusToolbar;
import jetbrains.communicator.idea.BaseToolWindow;
import jetbrains.communicator.idea.IDEAFacade;
import jetbrains.communicator.idea.IDEtalkContainerRegistry;
import jetbrains.communicator.idea.actions.DropDownButton;
import jetbrains.communicator.idea.actions.FindUsersAction;
import jetbrains.communicator.idea.actions.OptionsButton;
import jetbrains.communicator.util.UIUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"RefusedBequest", "RefusedBequest", "RefusedBequest"})
public class IDEtalkToolWindow extends BaseToolWindow implements JDOMExternalizable {

  @NonNls public static final String PLACE_TOOLBAR = "TOOLBAR";
  @NonNls private static final String TOOL_WINDOW_ID = "IDEtalk";

  private final UserListComponentImpl myUserListComponent;
  private final MutablePicoContainer myContainer;

  private JPanel myTopPanel;

  public IDEtalkToolWindow(ToolWindowManager toolWindowManager,
                           ActionManager actionManager, Project project,
                           IDEtalkContainerRegistry containerRegistry) {
    super(toolWindowManager, actionManager, project);
    myContainer = containerRegistry.getContainer();

    myContainer.registerComponentImplementation(UserListComponentImpl.class);
    myContainer.registerComponentImplementation(StatusToolbarImpl.class);

    myUserListComponent = (UserListComponentImpl) myContainer.getComponentInstanceOfType(UserListComponentImpl.class);
  }

  public void initComponent() {
    myTopPanel = new JPanel();
    myTopPanel.setLayout(new BoxLayout(myTopPanel, BoxLayout.Y_AXIS));
  }

  protected Component getComponent() {
    return myUserListComponent.getComponent();
  }

  protected String getToolWindowId() {
    return TOOL_WINDOW_ID;
  }

  private void initializeTransports(String projectName) {
    java.util.List transports = Pico.getInstance().getComponentInstancesOfType(Transport.class);
    for (Object transport1 : transports) {
      Transport transport = (Transport) transport1;
      transport.initializeProject(projectName, myContainer);
    }
  }

  public void projectClosed() {
    UIUtil.removeListenersToPreventMemoryLeak(((Container) getComponent()));
    super.projectClosed();
  }

  @NotNull
  public String getComponentName() {
    return "IDEtalkToolWindow";
  }

  public void readExternal(Element element) throws InvalidDataException {
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (myUserListComponent != null) {
      myUserListComponent.saveState();
    }
    throw new WriteExternalException();
  }

  protected void createToolWindowComponent() {

    StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
      public void run() {
        initializeTransports(myProject.getName());
      }
    });

    StatusToolbar statusToolbar = ((StatusToolbar) myContainer.getComponentInstanceOfType(StatusToolbar.class));

    ActionGroup toolbarActions = (ActionGroup) myActionManager.getAction("IDEtalk");
    ActionGroup treeActions = (ActionGroup) myActionManager.getAction("IDEtalk_Tree");

    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));

    toolbarActions = buildToolbarActions(toolbarPanel, toolbarActions);

    if (toolbarActions != null) {
      JComponent toolbar = createToolbar(toolbarActions).getComponent();
      toolbarPanel.add(toolbar);
    }

    toolbarPanel.add(Box.createHorizontalStrut(10));
    toolbarPanel.add(new SeparatorComponent(Color.lightGray, SeparatorOrientation.VERTICAL));
    toolbarPanel.add(Box.createHorizontalStrut(3));
    toolbarPanel.add(OptionsButton.wrap(new OptionsButton()));
    toolbarPanel.add(statusToolbar.createComponent());

    toolbarPanel.add(new JPanel() {
      public Dimension getPreferredSize() {
        return new Dimension(Short.MAX_VALUE, 10);
      }
    });

    if (treeActions != null) {
      JComponent component = createToolbar(treeActions).getComponent();
      component.setMinimumSize(component.getPreferredSize());
      toolbarPanel.add(component);
    }

    toolbarPanel.setAlignmentX(0);
    myTopPanel.add(toolbarPanel);

    myPanel.add(myTopPanel, BorderLayout.NORTH);
    myPanel.add(ScrollPaneFactory.createScrollPane(myUserListComponent.getComponent()));

    ActionGroup group = (ActionGroup)myActionManager.getAction("IDEtalkPopup");
    if (group != null) {
      IDEAFacade.installPopupMenu(group, myUserListComponent.getTree(), myActionManager);
    }
  }

  private ActionToolbar createToolbar(final ActionGroup toolbarActions) {
    final ActionToolbar toolbar = myActionManager.createActionToolbar(PLACE_TOOLBAR, toolbarActions, true);
    toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
    return toolbar;
  }

  private ActionGroup buildToolbarActions(JPanel toolbarPanel, ActionGroup toolbarActions) {
    FindUsersAction findUsersAction = new FindUsersAction();
    toolbarPanel.add(DropDownButton.wrap(new DropDownButton(findUsersAction, IconUtil.getAddIcon())));
    return toolbarActions;
  }

  protected ToolWindowAnchor getAnchor() {
    return ToolWindowAnchor.RIGHT;
  }

  public static void main(String[] args) {
    Box topPanel = Box.createHorizontalBox();
    topPanel.add(new JPanel() {
      {
        setOpaque(true);
        setBackground(JBColor.red);
      }
      public Dimension getPreferredSize() {
        return new Dimension(Short.MAX_VALUE, 10);
      }
    });

    topPanel.setPreferredSize(new Dimension(200, 30));
    UIUtil.run(topPanel);
  }

}
