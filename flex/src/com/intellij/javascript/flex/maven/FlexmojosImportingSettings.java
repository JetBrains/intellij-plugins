package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.project.AdditionalMavenImportingSettings;

import javax.swing.*;

public class FlexmojosImportingSettings implements AdditionalMavenImportingSettings {

  public UnnamedConfigurable createConfigurable(final Project project) {
    return new FlexmojosImportingConfigurable(project);
  }

  private static class FlexmojosImportingConfigurable implements UnnamedConfigurable {

    private final FlexCompilerProjectConfiguration myConfig;

    private JPanel myMainPanel;
    private JCheckBox myGenerateFlexCompilerConfigurationCheckBox;

    public FlexmojosImportingConfigurable(final Project project) {
      myConfig = FlexCompilerProjectConfiguration.getInstance(project);
    }

    public JComponent createComponent() {
      return myMainPanel;
    }

    public boolean isModified() {
      return myGenerateFlexCompilerConfigurationCheckBox.isSelected() != myConfig.GENERATE_FLEXMOJOS_CONFIGS;
    }

    public void apply() throws ConfigurationException {
      myConfig.GENERATE_FLEXMOJOS_CONFIGS = myGenerateFlexCompilerConfigurationCheckBox.isSelected();
    }

    public void reset() {
      myGenerateFlexCompilerConfigurationCheckBox.setSelected(myConfig.GENERATE_FLEXMOJOS_CONFIGS);
    }

    public void disposeUIResources() {
    }
  }
}
