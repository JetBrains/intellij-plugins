package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.AirSigningOptions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class KeystorePasswordDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JCheckBox myRememberPasswordsCheckBox;

  private final Project myProject;
  private final Collection<Trinity<AirSigningOptions, JPasswordField, JPasswordField>> myKeystoresAndPasswordFields;
  private JComponent myPreferredFocusedComponent;

  public KeystorePasswordDialog(final Project project, final Collection<AirSigningOptions> signingOptionsWithUnknownPasswords) {
    super(project);
    myProject = project;
    myKeystoresAndPasswordFields = createPasswordFields(signingOptionsWithUnknownPasswords);


    setTitle(FlexBundle.message("package.air.application.title"));
    init();
    myRememberPasswordsCheckBox.setSelected(PasswordStore.getInstance(project).isRememberPasswords());
  }

  private Collection<Trinity<AirSigningOptions, JPasswordField, JPasswordField>> createPasswordFields(final Collection<AirSigningOptions> signingOptionsWithUnknownPasswords) {
    final Collection<Trinity<AirSigningOptions, JPasswordField, JPasswordField>> result =
      new ArrayList<Trinity<AirSigningOptions, JPasswordField, JPasswordField>>();

    final JPanel panel = new JPanel(new GridBagLayout());
    myMainPanel.add(panel, BorderLayout.CENTER);

    int row = 0;
    for (AirSigningOptions signingOptions : signingOptionsWithUnknownPasswords) {
      if (row > 0) {
        panel.add(new JSeparator(SwingConstants.HORIZONTAL),
                  new GridBagConstraints(0, row, 2, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(5, 0, 5, 0), 0, 0));
        row++;
      }

      panel.add(new JLabel("Keystore file:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     new Insets(2, 0, 2, 0), 0, 0));
      panel.add(new JLabel(FileUtil.toSystemDependentName(signingOptions.getKeystorePath())),
                new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                       new Insets(2, 0, 2, 0), 0, 0));
      row++;

      panel.add(new JLabel("Keystore password:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                         GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
      final JPasswordField keystorePasswordField = new JPasswordField();
      panel.add(keystorePasswordField, new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                              new Insets(2, 0, 2, 0), 0, 0));

      if (row == 1) {
        myPreferredFocusedComponent = keystorePasswordField;
      }

      row++;

      if (signingOptions.getKeyAlias().isEmpty()) {
        result.add(Trinity.create(signingOptions, keystorePasswordField, (JPasswordField)null));
      }
      else {
        panel.add(new JLabel("Key alias:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                   new Insets(2, 0, 2, 0), 0, 0));
        panel.add(new JLabel(signingOptions.getKeyAlias()), new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                                   GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
        row++;

        panel.add(new JLabel("Key password:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
        final JPasswordField keyPasswordField = new JPasswordField();
        panel.add(keyPasswordField, new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                           new Insets(2, 0, 2, 0), 0, 0));
        row++;

        result.add(Trinity.create(signingOptions, keystorePasswordField, keyPasswordField));
      }
    }

    return result;
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myPreferredFocusedComponent;
  }

  protected ValidationInfo doValidate() {
    for (Trinity<AirSigningOptions, JPasswordField, JPasswordField> entry : myKeystoresAndPasswordFields) {
      final AirSigningOptions signingOptions = entry.first;
      final JPasswordField keystorePasswordField = entry.second;
      final String keystorePassword = new String(keystorePasswordField.getPassword());
      final JPasswordField keyPasswordField = entry.third;
      final String keyPassword = keyPasswordField == null ? "" : new String(keyPasswordField.getPassword());
      try {
        PasswordStore.checkPassword(signingOptions, keystorePassword, keyPassword);
      }
      catch (PasswordStore.SigningOptionsException e) {
        final JPasswordField errorField = e.wrongKeyPassword ? keyPasswordField : e.wrongKeystorePassword ? keystorePasswordField : null;
        final String message = errorField == null
                               ? PathUtil.getFileName(signingOptions.getKeystorePath()) + ": " + e.getMessage()
                               : e.getMessage();
        return new ValidationInfo(message, errorField);
      }
    }

    return null;
  }

  protected void doOKAction() {
    storePasswords();
    super.doOKAction();
  }

  private void storePasswords() {
    final boolean rememberPasswords = myRememberPasswordsCheckBox.isSelected();
    final PasswordStore passwordStore = PasswordStore.getInstance(myProject);

    passwordStore.setRememberPasswords(rememberPasswords);

    if (rememberPasswords) {
      storePasswords(passwordStore);
    }
    else {
      passwordStore.clearPasswords();
    }
  }

  private void storePasswords(final PasswordStore passwordStore) {
    for (Trinity<AirSigningOptions, JPasswordField, JPasswordField> entry : myKeystoresAndPasswordFields) {
      final AirSigningOptions signingOptions = entry.first;
      final String keystorePassword = new String(entry.second.getPassword());
      final JPasswordField keyPasswordField = entry.third;

      passwordStore.storeKeystorePassword(signingOptions.getKeystorePath(), keystorePassword);

      if (keyPasswordField != null) {
        final String keyPassword = new String(keyPasswordField.getPassword());
        passwordStore.storeKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias(), keyPassword);
      }
    }
  }

  public PasswordStore getPasswords() {
    assert isOK() : "ask for passwords only after OK in dialog";

    final PasswordStore passwordStore = PasswordStore.getInstance(myProject);
    if (passwordStore.isRememberPasswords()) return passwordStore;

    final PasswordStore temporaryStore = new PasswordStore();
    storePasswords(temporaryStore);
    return temporaryStore;
  }
}
