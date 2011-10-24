package com.intellij.flex.uiDesigner.testAssistant.run;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.DefaultJavaProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MyAstellaConfigurationType implements ConfigurationType {
  private final ConfigurationFactory factory;

  MyAstellaConfigurationType() {
    factory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new MyRunConfiguration(project, this, "");
      }
    };
  }

  @Override
  public String getDisplayName() {
    return "MyAstellaConfigurationType";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "MyAstellaConfigurationType";
  }

  private static final Icon ICON = IconLoader.getIcon("/runConfigurations/application.png");
  @Override
  public Icon getIcon() {
    return ICON;
  }

  @NotNull
  @Override
  public String getId() {
    return "com.intellij.flex.uiDesigner.testAssistant.run.MyAstellaConfigurationType";
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[] {factory};
  }

  private static class MyRunConfiguration extends RunConfigurationBase implements RunConfiguration {
    protected MyRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
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
      final CommandLineState state = new CommandLineState(env) {
        @NotNull
        @Override
        protected ProcessHandler startProcess() throws ExecutionException {
          final String commandLine;
          final Process process;
          try {
            commandLine = FileUtil.loadFile(new File(System.getProperty("user.home") + "/astella.run"));
            process = new ProcessBuilder(commandLine).start();
          }
          catch (IOException e) {
            throw new ExecutionException("", e);
          }

          return new DefaultJavaProcessHandler(process, commandLine, Charsets.UTF_8);
        }
      };

      state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
      return state;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
    }
  }

  private static class UnknownSettingsEditor extends SettingsEditor<MyRunConfiguration> {
    private final JPanel myPanel;

    private UnknownSettingsEditor() {
      myPanel = new JPanel();
      myPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));

      myPanel.add(new JLabel("This configuration can not be edited", JLabel.CENTER));
    }

    protected void resetEditorFrom(final MyRunConfiguration s) {
    }

    protected void applyEditorTo(final MyRunConfiguration s) throws ConfigurationException {
    }

    @NotNull
    protected JComponent createEditor() {
      return myPanel;
    }

    protected void disposeEditor() {
    }
  }
}