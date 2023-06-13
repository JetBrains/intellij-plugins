// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.AirSigningOptions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.JBInsets;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class KeystorePasswordDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JCheckBox myRememberPasswordsCheckBox;

  private final Project myProject;
  private final Collection<AuthForm> myKeystoresAndPasswordFields;
  private JComponent myPreferredFocusedComponent;

  public KeystorePasswordDialog(final Project project, final Collection<AirSigningOptions> signingOptionsWithUnknownPasswords) {
    super(project);
    myProject = project;
    myKeystoresAndPasswordFields = createPasswordFields(signingOptionsWithUnknownPasswords);


    setTitle(FlexBundle.message("package.air.application.title"));
    init();
    myRememberPasswordsCheckBox.setSelected(PasswordStore.getInstance(project).isRememberPasswords());
  }

  private Collection<AuthForm> createPasswordFields(final Collection<AirSigningOptions> signingOptionsWithUnknownPasswords) {
    final Collection<AuthForm> result =
      new ArrayList<>();

    final JPanel panel = new JPanel(new GridBagLayout());
    myMainPanel.add(panel, BorderLayout.CENTER);

    int row = 0;
    for (AirSigningOptions signingOptions : signingOptionsWithUnknownPasswords) {
      if (row > 0) {
        panel.add(new JSeparator(SwingConstants.HORIZONTAL),
                  new GridBagConstraints(0, row, 2, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         JBInsets.create(5, 0), 0, 0));
        row++;
      }

      panel.add(new JLabel("Keystore file:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     JBInsets.create(2, 0), 0, 0));
      panel.add(new JLabel(FileUtil.toSystemDependentName(signingOptions.getKeystorePath())),
                new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                       JBInsets.create(2, 0), 0, 0));
      row++;

      panel.add(new JLabel("Keystore password:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                         GridBagConstraints.NONE, JBInsets.create(2, 0), 0, 0));
      final JPasswordField keystorePasswordField = new JPasswordField();
      panel.add(keystorePasswordField, new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                              JBInsets.create(2, 0), 0, 0));

      if (row == 1) {
        myPreferredFocusedComponent = keystorePasswordField;
      }

      row++;

      if (signingOptions.getKeyAlias().isEmpty()) {
        result.add(new AuthForm(signingOptions, keystorePasswordField, null));
      }
      else {
        panel.add(new JLabel("Key alias:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                   JBInsets.create(2, 0), 0, 0));
        panel.add(new JLabel(signingOptions.getKeyAlias()), new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                                   GridBagConstraints.NONE, JBInsets.create(2, 0), 0, 0));
        row++;

        panel.add(new JLabel("Key password:"), new GridBagConstraints(0, row, 1, 1, 0., 0., GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, JBInsets.create(2, 0), 0, 0));
        final JPasswordField keyPasswordField = new JPasswordField();
        panel.add(keyPasswordField, new GridBagConstraints(1, row, 1, 1, 0., 0., GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                           JBInsets.create(2, 0), 0, 0));
        row++;

        result.add(new AuthForm(signingOptions, keystorePasswordField, keyPasswordField));
      }
    }

    return result;
  }
  
  private record AuthForm(AirSigningOptions signingOptions, JPasswordField keystorePasswordField, JPasswordField keyPasswordField) {}

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPreferredFocusedComponent;
  }

  @Override
  protected ValidationInfo doValidate() {
    for (AuthForm form : myKeystoresAndPasswordFields) {
      final AirSigningOptions signingOptions = form.signingOptions();
      final JPasswordField keystorePasswordField = form.keystorePasswordField();
      final String keystorePassword = new String(keystorePasswordField.getPassword());
      final JPasswordField keyPasswordField = form.keyPasswordField();
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

  @Override
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
    for (AuthForm form : myKeystoresAndPasswordFields) {
      final AirSigningOptions signingOptions = form.signingOptions();
      final String keystorePassword = new String(form.keystorePasswordField().getPassword());
      final JPasswordField keyPasswordField = form.keyPasswordField();

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
