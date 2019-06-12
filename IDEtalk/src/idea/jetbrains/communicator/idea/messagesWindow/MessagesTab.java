// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.messagesWindow;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.content.Content;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.BaseLocalMessage;
import jetbrains.communicator.idea.ConsoleMessage;
import jetbrains.communicator.idea.ConsoleUtil;
import jetbrains.communicator.idea.config.IdeaFlags;
import jetbrains.communicator.util.HardWrapUtil;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.TimeUtil;
import jetbrains.communicator.util.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Disposable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Date;

/**
 * @author Kir
 */
public class MessagesTab implements Disposable {
  private static final Key<MessagesTab> KEY = new Key<>("MESSAGES_TAB");

  @NonNls
  private static final String LF = "LineFeed";
  @NonNls
  private static final String SEND = "Send";

  private JPanel myMainPanel;
  private JTextArea myInput;
  private JButton mySend;
  private JPanel myMessages;
  private JSplitPane mySplitPane;

  private final Project myProject;
  private final ConsoleView myConsoleView;
  private final User myUser;
  private final HardWrapUtil myHardWrapUtil;
  private final DocumentListener myButtonsUpdater;
  private final LocalMessageDispatcher myLocalMessageDispatcher;
  private Content myContent;
  private IDEtalkAdapter myListener;

  public MessagesTab(Project project, User user, LocalMessageDispatcher localMessageDispatcher, boolean loadPreviousHistoryInNewTab) {
    myProject = project;
    myUser = user;
    myLocalMessageDispatcher = localMessageDispatcher;

    myConsoleView = createConsoleView(project);
    myMessages.add(myConsoleView.getComponent());
    myHardWrapUtil = new HardWrapUtil(myInput);

    myMainPanel.revalidate();

    myButtonsUpdater = new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        String s = myHardWrapUtil.getText();
        boolean enabled = !com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(s);
        mySend.setEnabled(enabled);
      }
    };
    mySend.addActionListener(new SendAction());
    myInput.getDocument().addDocumentListener(myButtonsUpdater);

    myInput.getActionMap().put(SEND, new SendAction());
    myInput.getActionMap().put(LF, new LfAction());

    updateKeyBindings();

    setupIDEtalkListener();

    setupDivider();

    if (loadPreviousHistoryInNewTab) {
      loadTodaysMessages();
    }
  }

  private void loadTodaysMessages() {
    LocalMessage[] history = myLocalMessageDispatcher.getHistory(myUser, TimeUtil.getDay(new Date()));
    for (LocalMessage localMessage : history) {
      ConsoleMessage consoleMessage = ((BaseLocalMessage) localMessage).createConsoleMessage(myUser);
      outputMessage(consoleMessage);
    }
  }

  private void setupIDEtalkListener() {
    myListener = new IDEtalkAdapter() {
      @Override
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){
          @Override public void visitSettingsChanged(SettingsChanged settingsChanged) {
            updateKeyBindings();
          }
        });
      }
    };

    myLocalMessageDispatcher.getBroadcaster().addListener(myListener);
  }

  private void setupDivider() {
    UIUtil.runWhenShown(mySplitPane, () -> mySplitPane.setDividerLocation(mySplitPane.getHeight() >> 1));
  }


  private void updateKeyBindings() {
    myInput.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));
    myInput.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

    if (IdeaFlags.USE_ENTER_FOR_MESSAGES.isSet()) {
      myInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), SEND);
      myInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), LF);
      mySend.setToolTipText(null);
    }
    else {
      myInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), SEND);

      mySend.setToolTipText(CommunicatorStrings.getMsg("use.ctrl.enter.to.send.message"));
    }
  }

  @Override
  public void dispose() {
    myLocalMessageDispatcher.getBroadcaster().removeListener(myListener);
    myInput.getDocument().removeDocumentListener(myButtonsUpdater);
    Disposer.dispose(myConsoleView);
  }

  public void requestFocus() {
    UIUtil.requestFocus(myInput);
  }

  public User getUser() {
    return myUser;
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  public void outputMessage(ConsoleMessage consoleMessage) {
    ConsoleUtil.outputMessage(consoleMessage, myProject, myConsoleView);
  }

  public void showAllIncomingMessages() {
    for (final Message pendingMessage : myLocalMessageDispatcher.getPendingMessages(myUser)) {
      myLocalMessageDispatcher.sendNow(myUser, pendingMessage);
    }
  }

  protected ConsoleView createConsoleView(Project project) {
    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    return builder.getConsole();
  }

  JButton getSendButton() {
    return mySend;
  }

  String getInput() {
    return myHardWrapUtil.getText();
  }

  void append(String s) {
    myInput.append(s);
  }

  private void send() {
    myUser.sendMessage(myInput.getText(), myLocalMessageDispatcher.getBroadcaster());
    myInput.setText("");
  }

  public void attachTo(@NotNull Content content) {
    content.setDisposer(() -> dispose());
    content.putUserData(KEY, this);
    myContent = content;
  }

  public Content getContent() {
    return myContent;
  }

  public static MessagesTab getTab(Content selectedContent) {
    if (selectedContent == null) return null;
    return selectedContent.getUserData(KEY);
  }

  private class SendAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      send();
      IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myInput, true));
    }
  }
  private class LfAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      myInput.setText(myInput.getText() + "\n");
    }
  }
}
