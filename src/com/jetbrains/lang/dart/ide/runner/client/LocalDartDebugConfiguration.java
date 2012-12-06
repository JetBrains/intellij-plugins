package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.ide.runner.client.ui.LocalDartConfigurationEditorForm;

/**
 * @author: Fedor.Korotkov
 */
public class LocalDartDebugConfiguration extends DartDebugConfigurationBase {
  private final ConfigurationFactory myConfigurationFactory;

  public LocalDartDebugConfiguration(Project project, ConfigurationFactory configurationFactory, String name) {
    super(name, new RunConfigurationModule(project), configurationFactory);
    myConfigurationFactory = configurationFactory;
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new LocalDartDebugConfiguration(getProject(), myConfigurationFactory, getName());
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new LocalDartConfigurationEditorForm(getProject());
  }
}
