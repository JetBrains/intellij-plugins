package com.intellij.deno.run;

import com.intellij.deno.DenoBundle;
import com.intellij.deno.DenoUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.debugger.JSDebuggerBundle;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfiguration;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfigurationBase;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfigurationEditor;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.nodeJs.NodeDebugProgramRunnerKt;
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class DenoRunConfiguration extends DebuggableProcessRunConfigurationBase implements NodeJSDebuggableConfiguration,
                                                                                           DebuggableProcessRunConfiguration,
                                                                                           RunProfileWithCompileBeforeLaunchOption {


  protected DenoRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
    setProgramParameters("run"); //default
  }

  @Override
  protected @Nullable String computeDefaultExePath() {
    return DenoUtil.INSTANCE.getDenoExecutablePath();
  }

  @Override
  protected @NotNull String getInputFileTitle() {
    return DenoBundle.message("deno.run.configuration.file");
  }

  @Override
  public String suggestedName() {
    return "Deno: " + PathUtilRt.getFileName(getInputPath());
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
          @Override
          protected void initComponents() {
            super.initComponents();
            inputPathLabel(DenoBundle.message("deno.run.configuration.file"))
              .programParametersLabel(DenoBundle.message("deno.run.configuration.arguments"))
              .exePathLabel(DenoBundle.message("deno.name"));
          }

          @Override
          protected void addComponents() {
            super.addComponents();
            fixFieldsOrder();
          }

          private void fixFieldsOrder() {
            Component[] components = getComponents();
            Component file = components[0];
            Component args = components[1];
            Component exec = components[components.length -1];
            removeAll();
            add(exec);
            add(args);
            add(file);
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
    int debugPort = NodeCommandLineUtil.findDebugPort(getProgramParameters());
    if (debugPort == -1) {
      try {
        debugPort = NetUtils.findAvailableSocketPort();
      }
      catch (IOException e) {
        throw new ExecutionException("Cannot find available port", e);
      }
    }

    return new InetSocketAddress(NodeCommandLineUtil.getNodeLoopbackAddress(), debugPort);
  }

  @Override
  public @Nullable NodeJsInterpreter getInterpreter() {
    String path = getExePath();
    return new DenoInterpreter(path == null ? computeDefaultExePath() : path);
  }

  @Override
  public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
    return new DenoRunState(environment, this);
  }
}
