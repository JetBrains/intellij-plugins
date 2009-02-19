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
package jetbrains.communicator.idea.messagesWindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.content.*;
import jetbrains.communicator.core.IDEtalkProperties;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.idea.BaseToolWindow;
import jetbrains.communicator.idea.ConsoleMessage;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;
import jetbrains.communicator.util.icons.EmptyIcon;
import jetbrains.communicator.util.icons.IconSizeWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.reflect.Field;

public class IDEtalkMessagesWindowImpl extends BaseToolWindow implements IDEtalkMessagesWindow {

  @NonNls
  public static final String PLACE_TOOLBAR = "MessageWindowToolbar";

  private Timer myIconBlinker;

  private TabbedPaneWrapper myTabsWrapper;
  private EventsProcessor myEventsProcessor;
  private static final Color NEW_MESSAGE_AVAILABLE_COLOR = new Color(0, 200, 0);

  private ContentFactory myContentFactory;
  private ContentManager myContentManager;

  public IDEtalkMessagesWindowImpl(ToolWindowManager toolWindowManager, ActionManager actionManager, Project project) {
    super(toolWindowManager, actionManager, project);
  }

  private TabbedPaneWrapper getWrapper(TabbedPaneContentUI contentUI) throws IllegalAccessException {
    Field[] fields = contentUI.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      if (field.getType() == TabbedPaneWrapper.class) return (TabbedPaneWrapper) field.get(contentUI);
    }
    return null;
  }

  protected String getToolWindowId() {
    return TOOL_WINDOW_ID;
  }

  @NotNull
  public String getComponentName() {
    return "IDEtalkMessagesWindowImpl";
  }

  protected void createToolWindowComponent() {
    myContentFactory = PeerFactory.getInstance().getContentFactory();
    TabbedPaneContentUI contentUI = new TabbedPaneContentUI(JTabbedPane.TOP);
    myContentManager = myContentFactory.createContentManager(contentUI, true, myProject);
    try {
      myTabsWrapper = getWrapper(contentUI);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    myContentManager.addContentManagerListener(new ContentManagerAdapter(){

      public void selectionChanged(ContentManagerEvent event) {
        super.selectionChanged(event);
        // Process tab switching:
        UIUtil.invokeLater(new Runnable() {
          public void run() {
            processMessagesOfVisibleTab();
          }
        });
      }
    });

    myPanel = new NonOpaquePanel(new BorderLayout());
    UIUtil.runWhenShown(myPanel, new Runnable() {
      public void run() {
        showAllTabsOfUsersWithMessages();
      }
    });
    myPanel.add(myContentManager.getComponent());

    AnAction toolbarGroup = myActionManager.getAction("IDEtalk.MessageWindowToolbar");
    if (toolbarGroup != null) {
      ActionToolbar toolbar = myActionManager.createActionToolbar(PLACE_TOOLBAR, (ActionGroup) toolbarGroup, false);
      myPanel.add(toolbar.getComponent(), BorderLayout.WEST);
    }
  }

  private void showAllTabsOfUsersWithMessages() {
    User[] usersWithMessages = getLocalDispatcher().getUsersWithMessages();
    for (User user : usersWithMessages) {
      processNewMessage(user);
    }

    processMessagesOfVisibleTab();
  }

  private void processMessagesOfVisibleTab() {
    MessagesTab selectedTab = getSelectedTab();
    if (selectedTab != null && selectedTab.getComponent().isShowing()) {
      myTabsWrapper.setForegroundAt(myTabsWrapper.getSelectedIndex(), Color.black);
      myTabsWrapper.setToolTipTextAt(myTabsWrapper.getSelectedIndex(), null);
      selectedTab.showAllIncomingMessages();
    }
  }

  @Nullable
  private MessagesTab getSelectedTab() {
    return MessagesTab.getTab(myContentManager.getSelectedContent());
  }

  public void initComponent() {
  }

  public void projectClosed() {
    myIconBlinker.stop();
    myEventsProcessor.dispose();

    UIUtil.removeListenersToPreventMemoryLeak(myPanel);

    super.projectClosed();
  }

  public void projectOpened() {
    super.projectOpened();

    installIconBlinker(myToolWindow);
    Pico pico=Pico.getInstance();

    myEventsProcessor = new EventsProcessor(this,
        (UserModel) pico.getComponentInstanceOfType(UserModel.class),
        getLocalDispatcher(),
        myProject
    );

    myToolWindow.installWatcher(myContentManager);
  }

  protected ToolWindowAnchor getAnchor() {
    return ToolWindowAnchor.BOTTOM;
  }

  public void deliverMessage(ConsoleMessage consoleMessage) {

    final User user = consoleMessage.getUser();

    MessagesTab messagesTab = createAndSetSelectedTab(user, false);
    messagesTab.outputMessage(consoleMessage);

    if (isFrameActive()) {
      final boolean wasInvisible = !myToolWindow.isVisible();
      myToolWindow.show(new Runnable() {
        public void run() {
          MessagesTab tab = getTabForUser(user);
          if (tab != null && wasInvisible) {
            tab.requestFocus();
          }
        }
      });
    }
  }

  private boolean isFrameActive() {
    return WindowManagerEx.getInstanceEx().getFrame(myProject).isActive();
  }

  private MessagesTab createMessagesTabIfNeeded(User user, boolean loadPreviousHistoryInNewTab) {
    MessagesTab messagesTab = getTabForUser(user);
    if (messagesTab == null) {
      messagesTab = new MessagesTab(myProject, user, getLocalDispatcher(), loadPreviousHistoryInNewTab);

      Content content = myContentFactory.createContent(messagesTab.getComponent(), user.getDisplayName(), true);
      messagesTab.attachTo(content);
      myContentManager.addContent(content);
    }
    return messagesTab;
  }

  private MessagesTab getTabForUser(User user) {
    MessagesTab messagesTab = null;
    for (Content content : myContentManager.getContents()) {
      MessagesTab tab = MessagesTab.getTab(content);
      if (tab.getUser().equals(user)) {
        messagesTab = tab;
        break;
      }
    }
    return messagesTab;
  }

  public void newMessageAvailable(final User from, @Nullable final TransportEvent event) {
    UIUtil.invokeLater( new Runnable() {
      public void run() {
        processNewMessage(from);
      }
    });
  }

  public void showUserTabAndRequestFocus(User user) {
    MessagesTab tab = createAndSetSelectedTab(user, true);
    processMessagesOfVisibleTab();
    tab.requestFocus();
  }

  @Nullable
  public User getSelectedUser() {
    MessagesTab selectedTab = getSelectedTab();
    return selectedTab != null ? selectedTab.getUser() : null;
  }

  public void appendInputText(User user, String message) {
    MessagesTab tab = getTabForUser(user);
    tab.append(message);
  }

  public Window getWindow() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length > 0 && projects[0] == myProject) {
      return WindowManagerEx.getInstanceEx().getFrame(myProject);
    }
    return null;
  }

  public boolean hasFocus() {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return focusOwner != null && myPanel != null && SwingUtilities.isDescendingFrom(focusOwner, myPanel);
  }

  private MessagesTab createAndSetSelectedTab(User user, boolean loadPreviousHistoryInNewTab) {
    MessagesTab tab = createMessagesTabIfNeeded(user, loadPreviousHistoryInNewTab);
    myContentManager.setSelectedContent(tab.getContent());
    return tab;
  }


  public void expandToolWindow() {
    super.expandToolWindow();
  }

  public void removeToolWindow() {
    myContentManager.removeAllContents(true);
  }

  private void processNewMessage(User from) {
    Content oldSelected = myContentManager.getSelectedContent();

    final MessagesTab tab = createMessagesTabIfNeeded(from, true);

    if (oldSelected != null && oldSelected != tab.getContent()) {
      int index = myContentManager.getIndexOfContent(tab.getContent());
      myTabsWrapper.setForegroundAt(index, NEW_MESSAGE_AVAILABLE_COLOR);
      myTabsWrapper.setToolTipTextAt(index, StringUtil.getMsg("new.messages.are.available"));
      myContentManager.setSelectedContent(tab.getContent());
    }

    UIUtil.runWhenShown(tab.getComponent(), new Runnable(){
      public void run() {
        tab.showAllIncomingMessages();
      }
    });
  }

  private void installIconBlinker(final ToolWindow toolWindow) {
    final LocalMessageDispatcher dispatcher = getLocalDispatcher();

    myIconBlinker = new Timer(UIUtil.BLINK_DELAY, new IconBlinker(dispatcher, toolWindow));

    myIconBlinker.start();
  }

  private static class IconBlinker implements ActionListener {
    private boolean myFlag;
    private final LocalMessageDispatcher myDispatcher;
    private final ToolWindow myToolWindow;

    IconBlinker(LocalMessageDispatcher dispatcher, ToolWindow toolWindow) {
      myDispatcher = dispatcher;
      myToolWindow = toolWindow;
    }

    public void actionPerformed(ActionEvent e) {
      final Icon blinkingIcon = myDispatcher.getBlinkingIcon();
      if (blinkingIcon == null) {
        myToolWindow.setIcon(WORKER_ICON);
      } else {
        if (System.getProperty(IDEtalkProperties.IDEA_IDE_TALK_NO_TOOLBAR_BLINK) == null) {
          myFlag = !myFlag;
        }

        if (myFlag) {
          myToolWindow.setIcon(new EmptyIcon(WORKER_ICON.getIconWidth(), WORKER_ICON.getIconHeight()));
        } else {
          myToolWindow.setIcon(new IconSizeWrapper(WORKER_ICON, blinkingIcon));
        }
      }
    }
  }

  private static class MyExpandListener implements HierarchyListener {
    private final JComponent myComponent;
    private final Runnable myRunOnExpand;

    MyExpandListener(JComponent component, Runnable runOnExpand) {
      myComponent = component;
      myRunOnExpand = runOnExpand;
    }

    public void hierarchyChanged(HierarchyEvent e) {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            if (myComponent.isShowing()) {
              myRunOnExpand.run();
            }
          }
        }, ModalityState.stateForComponent(myComponent));
      }
    }
  }
}
