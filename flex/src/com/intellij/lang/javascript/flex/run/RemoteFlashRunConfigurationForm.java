package com.intellij.lang.javascript.flex.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RemoteFlashRunConfigurationForm extends SettingsEditor<RemoteFlashRunConfiguration> {

  private JPanel myMainPanel;
  private BCCombo myBCCombo;

  private final Project myProject;

  public RemoteFlashRunConfigurationForm(final Project project) {
    myProject = project;
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final RemoteFlashRunConfiguration configuration) {
    myBCCombo.resetFrom(configuration.getRunnerParameters());
  }

  protected void applyEditorTo(final RemoteFlashRunConfiguration configuration) throws ConfigurationException {
    myBCCombo.applyTo(configuration.getRunnerParameters());
  }

  protected void disposeEditor() {
    myBCCombo.dispose();
  }

  private void createUIComponents() {
    myBCCombo = new BCCombo(myProject);
  }
}
