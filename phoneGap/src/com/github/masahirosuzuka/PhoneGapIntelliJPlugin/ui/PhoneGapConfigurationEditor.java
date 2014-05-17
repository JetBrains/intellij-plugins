package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * PhoneGapConfigurationEditor.java
 *
 * Created by Masahiro Suzuka on 2014/04/06.
 */
public class PhoneGapConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  private JPanel component;
  private JRadioButton phoneGapRadioButton;
  private JRadioButton cordovaRadioButton;
  private JRadioButton androidRadioButton;
  private JRadioButton iOSRadioButton;
  private JRadioButton windowsPhoneRadioButton;
  private JRadioButton rippleButton;
  private JCheckBox weinreCheckBox;
  private JCheckBox logCatCheckBox;
  private JTextArea runScript;

  public PhoneGapConfigurationEditor(Project project) {

    phoneGapRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        runScript.setText("");
        runScript.setText("phonegap");
      }
    });

    cordovaRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        runScript.setText("");
        runScript.setText("cordova");
      }
    });

    androidRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if (phoneGapRadioButton.isSelected() || cordovaRadioButton.isSelected()) {
          if (phoneGapRadioButton.isSelected()) {
            runScript.setText("");
            runScript.setText("phonegap run android");
          } else {
            runScript.setText("");
            runScript.setText("cordova run android");
          }
        }
      }
    });

    iOSRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if (phoneGapRadioButton.isSelected() || cordovaRadioButton.isSelected()) {
          if (phoneGapRadioButton.isSelected()) {
            runScript.setText("");
            runScript.setText("phonegap run ios");
          } else {
            runScript.setText("");
            runScript.setText("cordova run ios");
          }
        }
      }
    });

    /*
    windowsPhoneRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

      }
    });
    */

    rippleButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        runScript.setText("");
        runScript.setText("node server.js");
      }
    });
  }

  @Override
  protected void resetEditorFrom(PhoneGapRunConfiguration phoneGapRunConfiguration) {
    if (phoneGapRunConfiguration == null) { // Creating new configuration
      phoneGapRadioButton.setSelected(true);// PhoneGap is default SDK but user can choose Cordova
      androidRadioButton.setSelected(false);
      iOSRadioButton.setSelected(false);
      windowsPhoneRadioButton.setSelected(false);
      runScript.setText("");
    } else {
      if (phoneGapRunConfiguration.PHONEGAP_PATH.equals("phonegap")) {
        phoneGapRadioButton.setSelected(true);
      } else {
        cordovaRadioButton.setSelected(true);
      }

      if (phoneGapRunConfiguration.PHONEGAP_PLATFORM.equals("android")) {
        androidRadioButton.setSelected(true);
      } else if(phoneGapRunConfiguration.PHONEGAP_PLATFORM.equals("ios")) {
        iOSRadioButton.setSelected(true);
      }

      if (phoneGapRunConfiguration.PHONEGAP_PLATFORM.equals("ripple")) {
        rippleButton.setSelected(true);
      }

      runScript.setText(createScriptFromForm());
    }
  }

  @Override
  protected void applyEditorTo(PhoneGapRunConfiguration phoneGapRunConfiguration) throws ConfigurationException {

    if (phoneGapRadioButton.isSelected()) {
      phoneGapRunConfiguration.PHONEGAP_PATH = PhoneGapSettings.PHONEGAP_PATH;
    }

    if (cordovaRadioButton.isSelected()) {
      phoneGapRunConfiguration.PHONEGAP_PATH = PhoneGapSettings.CORDOVA_PATH;
    }

    if (androidRadioButton.isSelected()) {
      phoneGapRunConfiguration.PHONEGAP_PLATFORM = "android";
    }

    if (iOSRadioButton.isSelected()) {
      phoneGapRunConfiguration.PHONEGAP_PLATFORM = "ios";
    }

    if (rippleButton.isSelected()) {
      phoneGapRunConfiguration.PHONEGAP_PLATFORM = "ripple";
    }

    if (runScript.getText().length() == 0) {

    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return component;
  }

  private String createScriptFromForm() {
    return "";
  }
}
