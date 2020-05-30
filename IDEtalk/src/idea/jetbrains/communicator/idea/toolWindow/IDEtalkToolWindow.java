// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.toolWindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
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
import org.jetbrains.annotations.NonNls;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import java.awt.*;

public final class IDEtalkToolWindow extends BaseToolWindow implements SettingsSavingComponent {
  @NonNls public static final String PLACE_TOOLBAR = "TOOLBAR";
  @NonNls private static final String TOOL_WINDOW_ID = "IDEtalk";

  private final UserListComponentImpl myUserListComponent;
  private final MutablePicoContainer myContainer;

  private final JPanel myTopPanel;

  public IDEtalkToolWindow(ToolWindowManager toolWindowManager,
                           ActionManager actionManager, Project project,
                           IDEtalkContainerRegistry containerRegistry) {
    super(toolWindowManager, actionManager, project);

    myContainer = containerRegistry.getContainer();

    myContainer.registerComponentImplementation(UserListComponentImpl.class);
    myContainer.registerComponentImplementation(StatusToolbarImpl.class);

    myUserListComponent = (UserListComponentImpl)myContainer.getComponentInstanceOfType(UserListComponentImpl.class);

    myTopPanel = new JPanel();
    myTopPanel.setLayout(new BoxLayout(myTopPanel, BoxLayout.Y_AXIS));
  }

  private Component getComponent() {
    return myUserListComponent.getComponent();
  }

  @Override
  protected String getToolWindowId() {
    return TOOL_WINDOW_ID;
  }

  private void initializeTransports(String projectName) {
    for (Object transport1 : Pico.getInstance().getComponentInstancesOfType(Transport.class)) {
      Transport transport = (Transport) transport1;
      transport.initializeProject(projectName, myContainer);
    }
  }

  @Override
  public void projectClosed() {
    UIUtil.removeListenersToPreventMemoryLeak(((Container) getComponent()));
    super.projectClosed();
  }

  @Override
  public void save() {
    if (myUserListComponent != null) {
      myUserListComponent.saveState();
    }
  }

  @Override
  protected void createToolWindowComponent() {
    StartupManager.getInstance(myProject).registerPostStartupActivity(() -> initializeTransports(myProject.getName()));

    StatusToolbar statusToolbar = ((StatusToolbar) myContainer.getComponentInstanceOfType(StatusToolbar.class));

    DefaultActionGroup toolbarActions = new DefaultActionGroup();
    ActionGroup actions = (ActionGroup)myActionManager.getAction("IDEtalk");
    if (actions != null) {
      toolbarActions.addAll(actions);
    }

    ActionGroup treeActions = (ActionGroup) myActionManager.getAction("IDEtalk_Tree");

    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
    toolbarPanel.add(DropDownButton.wrap(new DropDownButton(new FindUsersAction(), IconUtil.getAddIcon())));
    toolbarPanel.add(createToolbar(toolbarActions).getComponent());

    toolbarPanel.add(Box.createHorizontalStrut(10));
    toolbarPanel.add(new SeparatorComponent(JBColor.LIGHT_GRAY, SeparatorOrientation.VERTICAL));
    toolbarPanel.add(Box.createHorizontalStrut(3));
    toolbarPanel.add(DropDownButton.wrap(new OptionsButton()));
    toolbarPanel.add(statusToolbar.createComponent());

    toolbarPanel.add(new JPanel() {
      @Override
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

  @Override
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
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(Short.MAX_VALUE, 10);
      }
    });

    topPanel.setPreferredSize(new Dimension(200, 30));
    UIUtil.run(topPanel);
  }

}
