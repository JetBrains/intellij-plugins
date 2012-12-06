package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.javascript.debugger.execution.RemoteJavaScriptDebugConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.ide.runner.client.ui.RemoteDartConfigurationEditorForm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class RemoteDartDebugConfiguration extends DartDebugConfigurationBase {
  private final ConfigurationFactory myConfigurationFactory;
  private List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> myMappings =
    new ArrayList<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean>();

  public RemoteDartDebugConfiguration(Project project, ConfigurationFactory configurationFactory, String name) {
    super(name, new RunConfigurationModule(project), configurationFactory);
    myConfigurationFactory = configurationFactory;
  }

  public List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> getMappings() {
    return myMappings;
  }

  public void setMappings(List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mappings) {
    myMappings = mappings;
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new RemoteDartDebugConfiguration(getProject(), myConfigurationFactory, getName());
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new RemoteDartConfigurationEditorForm(getProject());
  }
}
