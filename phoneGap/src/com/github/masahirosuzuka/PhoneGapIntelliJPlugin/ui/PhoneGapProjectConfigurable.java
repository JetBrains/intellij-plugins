package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * PhoneGapProjectConfigurable.java
 *
 * Created by Masahiro Suzuka on 2014/05/30.
 */
public class PhoneGapProjectConfigurable implements Configurable {

  private JPanel component;
  private JTextField textField1;
  private JList list1;
  private JButton installPhoneGapCordovaPluginButton;

  public PhoneGapProjectConfigurable() {

  }

  @Nls
  @Override
  public String getDisplayName() {
    return "PhoneGap/Cordova";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return component;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }
}
