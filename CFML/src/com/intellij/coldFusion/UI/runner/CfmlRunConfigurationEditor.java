// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.runner;

import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CfmlRunConfigurationEditor extends SettingsEditor<CfmlRunConfiguration> {
  private JPanel myMainPanel;
  private JTextField myWebPathField;
  private JPanel myBrowserSelectorPanel;
  private final BrowserSelector myBrowserSelector;

  public CfmlRunConfigurationEditor() {
    myBrowserSelector = new BrowserSelector();
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
  }

  @Override
  protected void resetEditorFrom(@NotNull CfmlRunConfiguration s) {
    CfmlRunnerParameters params = s.getRunnerParameters();
    myWebPathField.setText(params.getUrl());
    myBrowserSelector.setSelected(params.getCustomBrowser());
  }

  @Override
  protected void applyEditorTo(@NotNull CfmlRunConfiguration s) throws ConfigurationException {
    CfmlRunnerParameters params = s.getRunnerParameters();
    params.setUrl(myWebPathField.getText());

    params.setCustomBrowser(myBrowserSelector.getSelected());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return myMainPanel;
  }
}
