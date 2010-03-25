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
package jetbrains.communicator.jabber.register;

import com.intellij.ui.DocumentAdapter;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.jabber.AccountInfo;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.TextAcceptor;
import jetbrains.communicator.util.UIUtil;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

/**
 * @author Kir
 */
public class RegistrationForm {
  private static final String USE_EXISTING_ACCOUNT_KEY = "UseExistingAccount";
  private static final Logger LOG = Logger.getLogger(RegistrationForm.class);

  private JTextField myNickname;
  private JLabel myNicknameLabel;
  private JLabel myFirstNameLabel;
  private JTextField myFirstName;
  private JLabel myLastNameLabel;
  private JTextField myLastName;

  private JTextField myUserName;
  private JLabel myPasswordAgainLabel;
  private JPasswordField myPassword;
  private JPasswordField myPasswordAgain;

  private JComboBox myServer;
  private JTextField myPort;
  private JRadioButton myCreateNew;
  private JRadioButton myUseExisting;
  private JPanel myPanel;
  private JCheckBox myForceSSL;
  private JCheckBox myRememberPassword;

  private final JabberFacade myFacade;
  private final TextAcceptor myErrorLabel;
  private final IDEFacade myIdeFacade;
  public static String INITIAL_MESSAGE;

  public RegistrationForm(JabberFacade jabberFacade, IDEFacade ideFacade, TextAcceptor errorLabel) {
    myFacade = jabberFacade;
    myIdeFacade = ideFacade;
    myErrorLabel = errorLabel;

    setupFormActionsAndLF();
    setupFormData();
    setUseExisingAccount(Pico.getOptions().isSet(USE_EXISTING_ACCOUNT_KEY));

    myErrorLabel.setText(INITIAL_MESSAGE);
    INITIAL_MESSAGE = null;
  }

  private void setupFormData() {

    DefaultComboBoxModel comboBoxModel = ((DefaultComboBoxModel) myServer.getModel());
    String[] servers = myFacade.getServers();
    for (String server : servers) {
      comboBoxModel.addElement(server);
    }

    AccountInfo account = myFacade.getMyAccount();
    if (account != null) {
      setUsername(account.getUsername());
      setServer(account.getServer());
      setPort(account.getPort());
      setPassword(account.getPassword());
      setForceSSL(account.isForceSSL());
      setRememberPassword(account.shouldRememberPassword());
    }
  }

  private void setupFormActionsAndLF() {

    myUseExisting.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean useExisting = myUseExisting.isSelected();

        JComponent[] hideableFields = new JComponent[]{
          myNickname, myNicknameLabel,
          myFirstName, myFirstNameLabel,
          myLastName, myLastNameLabel,
          myPasswordAgain, myPasswordAgainLabel,
        };

        for (JComponent hideableField : hideableFields) {
          hideableField.setVisible(!useExisting);
        }

        Window window = SwingUtilities.getWindowAncestor(myPanel);
        if (window != null) {
          window.pack();
        }
      }
    });

    UIUtil.traverse(myPanel, new UIUtil.TraverseAction() {
      public boolean executeAndContinue(Component c) {
        if (c instanceof JTextComponent) {
          JTextComponent textComponent = (JTextComponent) c;
          textComponent.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
              myErrorLabel.setText(null);
            }
          });
        }
        return true;
      }
    });
  }
  
  public final void setUseExisingAccount(boolean useExistingAccount) {
    if (useExistingAccount) {
      myUseExisting.setSelected(true);
    }
    else {
      myCreateNew.setSelected(true);
    }
    Pico.getOptions().setOption(USE_EXISTING_ACCOUNT_KEY, useExistingAccount);
  }

  public String getUsername() {
    return myUserName.getText().trim();
  }

  public String getServer() {
    return (String) myServer.getSelectedItem();
  }

  public int getPort() {
    try {
      return Integer.parseInt(myPort.getText());
    } catch (NumberFormatException e) {
      LOG.info(e.getMessage(), e);
    }
    return 0;
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public boolean useExistingAccount() {
    return myUseExisting.isSelected();
  }

  public void cancel() {
    myFacade.getMyAccount().setLoginAllowed(false);
  }

  public void commit() {
    final String[] result = new String[1];
    setUseExisingAccount(myUseExisting.isSelected());

    myFacade.getMyAccount().setRememberPassword(shouldRememberPassword());
    myFacade.getMyAccount().setLoginAllowed(true);

    try {
      UIUtil.run(myIdeFacade, StringUtil.getMsg("jabber.connecting"), new Runnable() {
        public void run() {
          doLogin(result);
        }
      });
    } catch (CanceledException e) {
      result[0] = StringUtil.getMsg("connection.cancelled");
    }

    if (result[0] == null) {
      myFacade.saveSettings();
    }
    myErrorLabel.setText(result[0]);

    Pico.getOptions().setOption(USE_EXISTING_ACCOUNT_KEY, true);
  }

  private void doLogin(String[] result) {
    myFacade.disconnect();
    
    if (useExistingAccount()) {
      result[0] = myFacade.connect(getUsername(), getPassword(), getServer(), getPort(), isForceSSL());
      addErrorPrefixIfNeeded(result);
    } else {
      result[0] = checkPassword();

      if (result[0] == null) {
        result[0] = myFacade.createAccountAndConnect(getUsername(), getPassword(), getServer(), getPort(), isForceSSL());
        addErrorPrefixIfNeeded(result);
      }
      if (result[0] == null) {
        try {
          myFacade.setVCardInfo(myNickname.getText(),
              myFirstName.getText(), myLastName.getText());
        } catch (XMPPException e) {
          LOG.info(e.getMessage(), e);
        }
      }
    }
  }

  private void addErrorPrefixIfNeeded(String[] result) {
    if (result[0] != null) {
      result[0] = StringUtil.getMsg("error.0", result[0]);
    }
  }

  private String checkPassword() {
    if (!Arrays.equals(myPassword.getPassword(), myPasswordAgain.getPassword())) {
      return StringUtil.getMsg("jabber.password.mismatch");
    }
    if (myPassword.getPassword().length < 8) {
      return StringUtil.getMsg("jabber.password.short");
    }
    if (new String(myPassword.getPassword()).equals(getUsername())) {
      return StringUtil.getMsg("jabber.password.username");
    }
    return null;
  }

  String getPassword() {
    return new String(myPassword.getPassword());
  }

  public JComponent getPreferredFocusedComponent() {
    return myUserName;
  }

  public void setUsername(String userName) {
    myUserName.setText(userName);
  }

  public void setPassword(String pwd) {
    myPassword.setText(pwd);
  }

  public void setPort(int port) {
    myPort.setText(String.valueOf(port));
  }

  public void setServer(String server) {
    myServer.setSelectedItem(server);
  }

  void setServerIdx(int i) {
    myServer.setSelectedIndex(i);
  }

  public void setPasswordAgain(String s) {
    myPasswordAgain.setText(s);
  }

  public void setNickame(String s) {
    myNickname.setText(s);
  }

  public void setFirstName(String s) {
    myFirstName.setText(s);
  }

  public void setLastName(String s) {
    myLastName.setText(s);
  }

  public void setForceSSL(boolean force) {
    myForceSSL.setSelected(force);
  }

  public boolean isForceSSL() {
    return myForceSSL.isSelected();
  }

  public boolean shouldRememberPassword() {
    return myRememberPassword.isSelected();
  }

  public void setRememberPassword(boolean remember) {
    myRememberPassword.setSelected(remember);
  }
}
