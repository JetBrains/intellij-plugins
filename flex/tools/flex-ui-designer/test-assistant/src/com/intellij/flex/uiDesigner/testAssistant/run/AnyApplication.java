package com.intellij.flex.uiDesigner.testAssistant.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AnyApplication implements ConfigurationType {
  private final ConfigurationFactory factory;

  AnyApplication() {
    factory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new AnyApplicationConfiguration(project, this, "");
      }
    };
  }

  @Override
  public String getDisplayName() {
    return "AnyApplication";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "AnyApplication";
  }

  @Override
  public Icon getIcon() {
    return AllIcons.RunConfigurations.Application;
  }

  @NotNull
  @Override
  public String getId() {
    return "com.intellij.flex.uiDesigner.testAssistant.run.AnyApplication";
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{factory};
  }

  private static class AnyApplicationConfiguration extends RunConfigurationBase implements ModuleRunProfile {
    private static final Module[] EMPTY_MODULES = new Module[0];

    protected AnyApplicationConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
      super(project, factory, name);
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
      return new UnknownSettingsEditor();
    }

    @Override
    public JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
      return null;
    }

    @Override
    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
      return null;
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
      final JavaCommandLineState state = new JavaCommandLineState(env) {
        @Override
        protected JavaParameters createJavaParameters() throws ExecutionException {
          final JavaParameters params = new JavaParameters();
          params.setJdk(JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk());
          params.setMainClass("com.intellij.idea.Main");
          params.setWorkingDirectory(SystemProperties.getUserHome() + "/Documents/idea/bin/");

          try {
            final BufferedReader reader = new BufferedReader(new FileReader(new File(SystemProperties.getUserHome() + "/astella.run")));
            try {
              params.getVMParametersList().addParametersString(reader.readLine());
              reader.readLine();
              params.getClassPath().add(reader.readLine());

            }
            finally {
              reader.close();
            }
          }
          catch (IOException e) {
            throw new ExecutionException("Cannot run", e);
          }

          return params;
        }
      };

      state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
      return state;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
    }

    @NotNull
    @Override
    public Module[] getModules() {
      return EMPTY_MODULES;
    }
  }

  private static class UnknownSettingsEditor extends SettingsEditor<AnyApplicationConfiguration> {
    private final JPanel myPanel;

    private UnknownSettingsEditor() {
      myPanel = new JPanel();
      myPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
    }

    protected void resetEditorFrom(final AnyApplicationConfiguration s) {
    }

    protected void applyEditorTo(final AnyApplicationConfiguration s) throws ConfigurationException {
    }

    @NotNull
    protected JComponent createEditor() {
      return myPanel;
    }

    protected void disposeEditor() {
    }
  }
}
