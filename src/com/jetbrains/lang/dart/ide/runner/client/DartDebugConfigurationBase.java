package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartDebugConfigurationBase extends ModuleBasedConfiguration<RunConfigurationModule>
  implements RunConfigurationWithSuppressedDefaultRunAction {
  private String myFileUrl = null;

  public DartDebugConfigurationBase(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
    super(name, configurationModule, factory);
  }

  @Override
  public Collection<Module> getValidModules() {
    Module[] modules = ModuleManager.getInstance(getProject()).getModules();
    return Arrays.asList(modules);
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    return new RunProfileState() {
      public ExecutionResult execute(final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
        return new ExecutionResult() {
          public ExecutionConsole getExecutionConsole() {
            return null;
          }

          public AnAction[] getActions() {
            return AnAction.EMPTY_ARRAY;
          }

          public ProcessHandler getProcessHandler() {
            return null;
          }
        };
      }

      public RunnerSettings getRunnerSettings() {
        return null;
      }

      public ConfigurationPerRunnerSettings getConfigurationSettings() {
        return null;
      }
    };
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);
    XmlSerializer.serializeInto(this, element);
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);
    XmlSerializer.deserializeInto(this, element);
  }

  public String getFileUrl() {
    return myFileUrl;
  }

  public void setFileUrl(@Nullable String fileUrl) {
    myFileUrl = fileUrl;
  }
}
