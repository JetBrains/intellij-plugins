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
package jetbrains.communicator.jabber.impl;

import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kir
 */
public class FindByJabberIdDialog extends IdeaDialog {
  private JTextArea myJabberIDs;
  private JComboBox myGroup;
  private JLabel myWelcomeText;
  private JPanel myPanel;
  private JLabel myGroupLabel;

  public FindByJabberIdDialog(List<String> groups) {
    super(false);
    setModal(true);

    setTitle(StringUtil.getMsg("jabber.findByIdCommandName.dialogTitle"));
    myWelcomeText.setText(StringUtil.getMsg("jabber.findByIdCommandName.dialogText"));
    setOKButtonText(StringUtil.getMsg("add"));

    UIUtil.setMnemonic(myWelcomeText, myJabberIDs, 'E');
    UIUtil.setMnemonic(myGroupLabel, myGroup, 'S');

    setupGroups(groups);
    init();
  }

  private void setupGroups(List<String> groups) {
    ((DefaultComboBoxModel) myGroup.getModel()).addElement(UserModel.DEFAULT_GROUP);
    for (Iterator<String> it = groups.iterator(); it.hasNext();) {
      ((DefaultComboBoxModel) myGroup.getModel()).addElement(it.next());
    }
  }

  public JComponent getPreferredFocusedComponent() {
    return myJabberIDs;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public String getGroup() {
    return (String) myGroup.getSelectedItem();
  }

  public String getJabberIDs() {
    return myJabberIDs.getText().trim();
  }

}
