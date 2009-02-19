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
package jetbrains.communicator.idea.sendMessage;

import jetbrains.communicator.commands.SendMessageInvoker;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.util.HardWrapUtil;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kir
 */
public class SendMessageDialog extends IdeaDialog {
  private JPanel myMainPanel;

  private JList myRecepients;

  private JLabel myRecepientsLabel;
  private JLabel myCommentLabel;
  private JTextArea myComment;
  private final SendMessageInvoker myRunOnOK;
  private final HardWrapUtil myWrapper;

  public SendMessageDialog(User[] availableUsers, User[] selectedUsers, String message, SendMessageInvoker runOnOK) {
    super(false);
    setModal(false);

    setTitle(" Send Message/Stacktrace ");

    setOKButtonText("Send");
    getOKAction().putValue(Action.MNEMONIC_KEY, new Integer((int) 'S'));

    ArrayList<User> users = new ArrayList<User>(Arrays.asList(availableUsers));
    UIUtil.setupUserList(myRecepients, users);
    UIUtil.setDefaultSelection(myRecepients, selectedUsers);

    myRecepientsLabel.setLabelFor(myRecepients);
    myCommentLabel.setLabelFor(myComment);

    if (!StringUtil.isEmpty(message)) {
      myComment.setText('\n' + message);
    }

    myRunOnOK = runOnOK;

    myWrapper = new HardWrapUtil(myComment);

    init();
  }

  public JComponent getPreferredFocusedComponent() {
    myComment.setCaretPosition(0);
    return myComment;
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    myRunOnOK.doSendMessage(getUsers(), getComment());
    super.doOKAction();
  }

  private User[] getUsers() {
    List list = Arrays.asList(myRecepients.getSelectedValues());
    return (User[]) list.toArray(new User[list.size()]);
  }

  private String getComment() {
    return myWrapper.getText();
  }

}
