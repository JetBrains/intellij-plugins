package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Masahiro Suzuka on 2014/05/05.
 */
public class PhoneGapSettingDialog implements Configurable{

  private JPanel components;

  @Nls
  @Override
  public String getDisplayName() {
    return "PhoneGap";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return components;
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
