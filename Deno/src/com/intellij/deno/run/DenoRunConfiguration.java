package com.intellij.deno.run;

import com.intellij.deno.DenoBundle;
import com.intellij.deno.DenoSettings;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfiguration;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfigurationBase;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfigurationEditor;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.lang.javascript.buildTools.base.JsbtUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.PathUtilRt;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.nodeJs.NodeDebugProgramRunnerKt;
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Objects;

public class DenoRunConfiguration extends DebuggableProcessRunConfigurationBase implements NodeJSDebuggableConfiguration,
                                                                                           DebuggableProcessRunConfiguration,
                                                                                           RunProfileWithCompileBeforeLaunchOption {

  private String applicationArguments;

  protected DenoRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
    setProgramParameters("run"); //default
  }

  @Override
  protected @Nullable String computeDefaultExePath() {
    return DenoSettings.Companion.getService(getProject()).getDenoPath();
  }

  @Override
  protected @NotNull String getInputFileTitle() {
    return DenoBundle.message("deno.run.configuration.file");
  }

  @Override
  public String suggestedName() {
    String inputPath = getInputPath();
    @NlsSafe String fileToRun = PathUtilRt.getFileName(inputPath);
    return DenoBundle.message("deno.run.configuration.default.name", fileToRun);
  }

  public void setApplicationArguments(String newArguments) {
    applicationArguments = newArguments;
  }
  
  @Nullable
  public String getApplicationArguments() {
    return applicationArguments;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();

    checkExePath(DenoBundle.message("deno.name"));
  }


  @Override
  public @NotNull XDebugProcess createDebugProcess(@NotNull InetSocketAddress socketAddress,
                                                   @NotNull XDebugSession session,
                                                   @Nullable ExecutionResult executionResult,
                                                   @NotNull ExecutionEnvironment environment) {
    return NodeDebugProgramRunnerKt.createDebugProcess(this, socketAddress, session, executionResult);
  }

  @Override
  public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DebuggableProcessRunConfigurationEditor<DenoRunConfiguration>(getProject()) {

      @NotNull
      @Override
      protected DebuggableProgramParametersPanel createEditor() {
        panel = new DebuggableProgramParametersPanel(getProject(), createExePathDescriptor(), createInputPathDescriptor()) {

          private LabeledComponent<RawCommandLineEditor> myApplicationParametersComponent;
          private RawCommandLineEditor myApplicationParametersTextField;

          @Override
          public void applyTo(CommonProgramRunConfigurationParameters c) {
            super.applyTo(c);
            ((DenoRunConfiguration)c).setApplicationArguments(myApplicationParametersTextField.getText());
          }

          @Override
          public void reset(CommonProgramRunConfigurationParameters c) {
            super.reset(c);
            myApplicationParametersTextField.setText(((DenoRunConfiguration)c).getApplicationArguments());
          }

          @Override
          protected void setupAnchor() {
            super.setupAnchor();
            myAnchor = UIUtil.mergeComponentsWithAnchor(this, myApplicationParametersComponent);
          }

          @Override
          protected void initComponents() {
            myApplicationParametersTextField = new RawCommandLineEditor();
            CommonProgramParametersPanel.addMacroSupport(myApplicationParametersTextField.getEditorField());
            JsbtUtil.resetFontToDefault(myApplicationParametersTextField);
            myApplicationParametersComponent =
              LabeledComponent.create(myApplicationParametersTextField, DenoBundle.message("deno.application.arguments"),
                                      BorderLayout.WEST);
            super.initComponents();
            inputPathLabel(DenoBundle.message("deno.run.configuration.file"))
              .programParametersLabel(DenoBundle.message("deno.run.configuration.arguments"))
              .exePathLabel(DenoBundle.message("deno.name"));
          }

          @Override
          protected void addComponents() {
            super.addComponents();
            fixFieldsAndOrder();
          }

          private void fixFieldsAndOrder() {
            Component[] components = getComponents();
            Component file = components[0];
            Component args = components[1];
            Component exec = components[components.length - 1];
            removeAll();
            add(exec);
            add(args);
            add(file);
            add(myApplicationParametersComponent);
            for (Component component : components) {
              if (component == file || component == exec || component == args) continue;
              add(component);
            }
          }
        };
        return panel;
      }
    };
  }

  @NotNull
  @Override
  public InetSocketAddress computeDebugAddress(RunProfileState state) throws ExecutionException {
    return NodeDebugProgramRunnerKt.computeDebugAddress(this);
  }

  @Override
  public int getConfiguredDebugPort() {
    return NodeCommandLineUtil.findDebugPort(getProgramParameters());
  }

  @Override
  public @Nullable NodeJsInterpreter getInterpreter() {
    String path = getExePath();
    return new DenoInterpreter(path == null ? Objects.requireNonNull(computeDefaultExePath()) : path);
  }

  @Override
  public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
    return new DenoRunState(environment, this);
  }
}
