// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.messagesWindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.content.*;
import com.intellij.util.ui.TimerUtil;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.IDEtalkProperties;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.idea.BaseToolWindow;
import jetbrains.communicator.idea.ConsoleMessage;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.util.CommunicatorStrings;
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
import java.lang.reflect.Field;

public final class IDEtalkMessagesWindowImpl extends BaseToolWindow implements IDEtalkMessagesWindow {
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

  private static TabbedPaneWrapper getWrapper(TabbedPaneContentUI contentUI) throws IllegalAccessException {
    Field[] fields = contentUI.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      if (field.getType() == TabbedPaneWrapper.class) return (TabbedPaneWrapper) field.get(contentUI);
    }
    return null;
  }

  @Override
  protected String getToolWindowId() {
    return TOOL_WINDOW_ID;
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "IDEtalkMessagesWindowImpl";
  }

  @Override
  protected void createToolWindowComponent() {
    myContentFactory = ContentFactory.SERVICE.getInstance();
    TabbedPaneContentUI contentUI = new TabbedPaneContentUI(SwingConstants.TOP);
    myContentManager = myContentFactory.createContentManager(contentUI, true, myProject);
    try {
      myTabsWrapper = getWrapper(contentUI);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    myContentManager.addContentManagerListener(new ContentManagerAdapter(){

      @Override
      public void selectionChanged(@NotNull ContentManagerEvent event) {
        super.selectionChanged(event);
        // Process tab switching:
        UIUtil.invokeLater(() -> processMessagesOfVisibleTab());
      }
    });

    myPanel = new NonOpaquePanel(new BorderLayout());
    UIUtil.runWhenShown(myPanel, () -> showAllTabsOfUsersWithMessages());
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
      myTabsWrapper.setForegroundAt(myTabsWrapper.getSelectedIndex(), com.intellij.util.ui.UIUtil.getListForeground());
      myTabsWrapper.setToolTipTextAt(myTabsWrapper.getSelectedIndex(), null);
      selectedTab.showAllIncomingMessages();
    }
  }

  @Nullable
  private MessagesTab getSelectedTab() {
    return MessagesTab.getTab(myContentManager.getSelectedContent());
  }

  @Override
  public void projectClosed() {
    if (myIconBlinker != null) {
      myIconBlinker.stop();
      myEventsProcessor.dispose();

      UIUtil.removeListenersToPreventMemoryLeak(myPanel);
    }

    super.projectClosed();
  }

  @Override
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

  @Override
  protected ToolWindowAnchor getAnchor() {
    return ToolWindowAnchor.BOTTOM;
  }

  @Override
  public void deliverMessage(ConsoleMessage consoleMessage) {

    final User user = consoleMessage.getUser();

    MessagesTab messagesTab = createAndSetSelectedTab(user, false);
    messagesTab.outputMessage(consoleMessage);

    if (isFrameActive()) {
      final boolean wasInvisible = !myToolWindow.isVisible();
      myToolWindow.show(() -> {
        MessagesTab tab = getTabForUser(user);
        if (tab != null && wasInvisible) {
          tab.requestFocus();
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

  @Override
  public void newMessageAvailable(final User from, @Nullable final TransportEvent event) {
    UIUtil.invokeLater(() -> processNewMessage(from));
  }

  @Override
  public void showUserTabAndRequestFocus(User user) {
    MessagesTab tab = createAndSetSelectedTab(user, true);
    processMessagesOfVisibleTab();
    tab.requestFocus();
  }

  @Override
  @Nullable
  public User getSelectedUser() {
    MessagesTab selectedTab = getSelectedTab();
    return selectedTab != null ? selectedTab.getUser() : null;
  }

  @Override
  public void appendInputText(User user, String message) {
    MessagesTab tab = getTabForUser(user);
    tab.append(message);
  }

  @Override
  public Window getWindow() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length > 0 && projects[0] == myProject) {
      return WindowManagerEx.getInstanceEx().getFrame(myProject);
    }
    return null;
  }

  @Override
  public boolean hasFocus() {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return focusOwner != null && myPanel != null && SwingUtilities.isDescendingFrom(focusOwner, myPanel);
  }

  private MessagesTab createAndSetSelectedTab(User user, boolean loadPreviousHistoryInNewTab) {
    MessagesTab tab = createMessagesTabIfNeeded(user, loadPreviousHistoryInNewTab);
    myContentManager.setSelectedContent(tab.getContent());
    return tab;
  }


  @Override
  public void removeToolWindow() {
    myContentManager.removeAllContents(true);
  }

  private void processNewMessage(User from) {
    Content oldSelected = myContentManager.getSelectedContent();

    final MessagesTab tab = createMessagesTabIfNeeded(from, true);

    if (oldSelected != null && oldSelected != tab.getContent()) {
      int index = myContentManager.getIndexOfContent(tab.getContent());
      myTabsWrapper.setForegroundAt(index, NEW_MESSAGE_AVAILABLE_COLOR);
      myTabsWrapper.setToolTipTextAt(index, CommunicatorStrings.getMsg("new.messages.are.available"));
      myContentManager.setSelectedContent(tab.getContent());
    }

    UIUtil.runWhenShown(tab.getComponent(), () -> tab.showAllIncomingMessages());
  }

  private void installIconBlinker(final ToolWindow toolWindow) {
    final LocalMessageDispatcher dispatcher = getLocalDispatcher();

    myIconBlinker = TimerUtil.createNamedTimer("IDETalk icon blinker", UIUtil.BLINK_DELAY, new IconBlinker(dispatcher, toolWindow));
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

    @Override
    public void actionPerformed(ActionEvent e) {
      final Icon blinkingIcon = myDispatcher.getBlinkingIcon();
      if (blinkingIcon == null) {
        myToolWindow.setIcon(IdeTalkCoreIcons.IdeTalk.User_toolwindow);
      }
      else {
        if (System.getProperty(IDEtalkProperties.IDEA_IDE_TALK_NO_TOOLBAR_BLINK) == null) {
          myFlag = !myFlag;
        }

        if (myFlag) {
          myToolWindow.setIcon(new EmptyIcon(13, 13));
        }
        else {
          myToolWindow.setIcon(new IconSizeWrapper(IdeTalkCoreIcons.IdeTalk.User_toolwindow, blinkingIcon));
        }
      }
    }
  }
}