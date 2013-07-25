package com.intellij.coldFusion.UI.runner;

import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigurationEditor extends SettingsEditor<CfmlRunConfiguration> {
  private JPanel myMainPanel;
  private JTextField myWebPathField;
  private JPanel myBrowserSelectorPanel;
  private BrowserSelector myBrowserSelector;

  public CfmlRunConfigurationEditor() {
    myBrowserSelector = new BrowserSelector(true);
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
  }

  @Override
  protected void resetEditorFrom(CfmlRunConfiguration s) {
    final CfmlRunnerParameters params = s.getRunnerParameters();
    myWebPathField.setText(params.getUrl());
    myBrowserSelector.setSelectedBrowser(params.getNonDefaultBrowser() != null ? params.getNonDefaultBrowser() : null);
  }

  @Override
  protected void applyEditorTo(CfmlRunConfiguration s) throws ConfigurationException {
    CfmlRunnerParameters params = s.getRunnerParameters();
    params.setUrl(myWebPathField.getText());
    params.setNonDefaultBrowser(myBrowserSelector.getSelectedBrowser());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  @Override
  protected void disposeEditor() {
  }
}
