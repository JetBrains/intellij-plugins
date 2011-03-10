package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Maxim
 * Date: 27.09.2010
 * Time: 17:32:09
 */
public class ActionScriptProfileSettings extends SettingsEditor<ProfileSettings> {
  private JPanel myPanel;
  private JTextField host;
  private JTextField port;

  @Override
  protected void resetEditorFrom(ProfileSettings profileSettings) {
    host.setText(profileSettings.getHost());
    port.setText(String.valueOf(profileSettings.getPort()));
  }

  @Override
  protected void applyEditorTo(ProfileSettings profileSettings) throws ConfigurationException {
    profileSettings.setHostFromString(host.getText());
    profileSettings.setPortFromString(port.getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }

  @Override
  protected void disposeEditor() {
    myPanel = null;
  }
}
