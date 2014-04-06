package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapRunConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by masahiro on 2014/04/06.
 */
public class PhoneGapConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  private JPanel component;
  private JRadioButton androidRadioButton;
  private JRadioButton iOSRadioButton;
  private JRadioButton windowsPhoneRadioButton;
  private JTextArea runScript;

  public PhoneGapConfigurationEditor(Project project) {
    androidRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        runScript.setText("");
        runScript.setText("phonegap run android");
      }
    });

    iOSRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        runScript.setText("");
        runScript.setText("phonegap run ios");
      }
    });

    /*
    windowsPhoneRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

      }
    });
    */
  }

  @Override
  protected void resetEditorFrom(PhoneGapRunConfiguration phoneGapRunConfiguration) {
    androidRadioButton.setSelected(false);
    iOSRadioButton.setSelected(false);
    windowsPhoneRadioButton.setSelected(false);
    runScript.setText("");
  }

  @Override
  protected void applyEditorTo(PhoneGapRunConfiguration phoneGapRunConfiguration) throws ConfigurationException {
    if (runScript.getText().length() == 0) {
      ;
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return component;
  }
}
